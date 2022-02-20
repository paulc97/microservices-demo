package hipstershop;

import static java.lang.Math.toIntExact;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import hipstershop.grpc.RatingServiceGrpc;
import hipstershop.grpc.RatingServiceOuterClass;
import hipstershop.persistence.PersistenceService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.health.v1.HealthCheckResponse.ServingStatus;
import io.grpc.services.HealthStatusManager;
import io.grpc.stub.StreamObserver;
import io.opencensus.common.Duration;
import io.opencensus.contrib.grpc.metrics.RpcViews;
import io.opencensus.exporter.stats.stackdriver.StackdriverStatsConfiguration;
import io.opencensus.exporter.stats.stackdriver.StackdriverStatsExporter;
import io.opencensus.exporter.trace.jaeger.JaegerExporterConfiguration;
import io.opencensus.exporter.trace.jaeger.JaegerTraceExporter;
import io.opencensus.exporter.trace.stackdriver.StackdriverTraceConfiguration;
import io.opencensus.exporter.trace.stackdriver.StackdriverTraceExporter;
import io.opencensus.trace.Span;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;

public class GrpcServer {
	
	private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	
	public static void main(String[] args) throws IOException, InterruptedException {
		
		logger.setLevel(Level.ALL);
		try {
            setupOpenCensusAndExporters();
        } catch (IOException e) {
        	logger.info("Failed to setup OpenCensus exporters: " + e + " so proceeding without it");
        }
		startService();
		
		
	}
	private static void startService() throws IOException, InterruptedException {

				Server server;
				HealthStatusManager healthMgr;
				healthMgr = new HealthStatusManager();
				server = ServerBuilder.forPort(9090).addService(new RatingService()).addService(healthMgr.getHealthService()).build();
				server.start();
				logger.info("Server started");
				healthMgr.setStatus("", ServingStatus.SERVING);
				server.awaitTermination();
		
	}
	static class RatingService extends RatingServiceGrpc.RatingServiceImplBase {
		private static final Tracer tracer = Tracing.getTracer();
		
		private final hipstershop.persistence.PersistenceService persistenceService = new PersistenceService();
		
		@Override
		public void rateProduct(RatingServiceOuterClass.ProductRequest request, StreamObserver<RatingServiceOuterClass.ApiResponse> responseObserver) {
			Span span = RatingService.tracer.spanBuilder("(*RatingServive).rateProduct").setRecordEvents(true).startSpan();
			
			persistenceService.saveProductRating(request.getProductId(), toIntExact(request.getRating()));
			RatingServiceOuterClass.ApiResponse.Builder response = RatingServiceOuterClass.ApiResponse.newBuilder();
			
			response.setResponseMessage("SUCCESS: product rating saved");
			responseObserver.onNext(response.build());
			responseObserver.onCompleted();
			span.end();
		}
		@Override
		public void getProductRating(RatingServiceOuterClass.ProductRatingRequest request, StreamObserver<RatingServiceOuterClass.ProductRatingResponse> responseObserver) {
			Span span = RatingService.tracer.spanBuilder("(*RatingServive).getProdcutRating").setRecordEvents(true).startSpan();
			
			RatingServiceOuterClass.ProductRatingResponse.Builder response = RatingServiceOuterClass.ProductRatingResponse.newBuilder();
			
			response.setRating(persistenceService.getProductRating(request.getProductId()));
			response.setRatingCount(persistenceService.getNumberOfProductRatings(request.getProductId()));
			response.setResponseMessage("SUCESS: rating send");
			responseObserver.onNext(response.build());
			responseObserver.onCompleted();
			span.end();
		}
		@Override
		public void rateShop(RatingServiceOuterClass.ShopRequest request, StreamObserver<RatingServiceOuterClass.ApiResponse> responseObserver) {
			Span span = RatingService.tracer.spanBuilder("(*RatingServive).rateShop").setRecordEvents(true).startSpan();
			persistenceService.saveShopRating(toIntExact(request.getRating()));
			RatingServiceOuterClass.ApiResponse.Builder response = RatingServiceOuterClass.ApiResponse.newBuilder();
			
			response.setResponseMessage("SUCCESS: shop rating saved");
			responseObserver.onNext(response.build());
			responseObserver.onCompleted();
			span.end();
		}
		@Override
		public void getShopRating(RatingServiceOuterClass.Empty request, StreamObserver<RatingServiceOuterClass.ShopRatingResponse> responseObserver) {
			Span span = RatingService.tracer.spanBuilder("(*RatingServive).getShopRating").setRecordEvents(true).startSpan();
			RatingServiceOuterClass.ShopRatingResponse.Builder response = RatingServiceOuterClass.ShopRatingResponse.newBuilder();
			
			response.setRating(persistenceService.getShopRating());
			response.setRatingCount(persistenceService.getNumberOfShopRatings());
			response.setResponseMessage("SUCESS: rating send");
			responseObserver.onNext(response.build());
			responseObserver.onCompleted();
			span.end();
		}
	}
	private static void initStats() {

	    long sleepTime = 2; /* seconds */
	    int maxAttempts = 3;
	    boolean statsExporterRegistered = false;
	    for (int i = 0; i < maxAttempts; i++) {
	      try {
	        if (!statsExporterRegistered) {
	        
	          StackdriverStatsExporter.createAndRegister(
	              StackdriverStatsConfiguration.builder()
	              	.setProjectId("RatingService")
	                  .setExportInterval(Duration.create(10, 0))
	                  .build());
	          statsExporterRegistered = true;
	        }
	      } catch (Exception e) {
	        if (i == (maxAttempts - 1)) {
	        	
	        } else {
	               try {
	            Thread.sleep(TimeUnit.SECONDS.toMillis(sleepTime));
	          } catch (Exception se) {
	           
	          }
	        }
	      }
	    }
	  }
	private static void initTracing() {
	    if (System.getenv("DISABLE_TRACING") != null) {
	      logger.info("Tracing disabled.");
	      return;
	    }
	    logger.info("Tracing enabled");

	    long sleepTime = 3; /* seconds */
	    int maxAttempts = 3;
	    boolean traceExporterRegistered = false;

	    for (int i = 0; i < maxAttempts; i++) {
	      try {
	        if (!traceExporterRegistered) {
	          StackdriverTraceExporter.createAndRegister(
	              StackdriverTraceConfiguration.builder().build());
	          traceExporterRegistered = true;
	        }
	      } catch (Exception e) {
	        if (i == (maxAttempts - 1)) {
	          logger.warning(
	              "Failed to register Stackdriver Exporter."
	                  + " Tracing data will not reported to Stackdriver. Error message: "
	                  + e.toString());
	        } else {
	          logger.info("Attempt to register Stackdriver Exporter in " + sleepTime + " seconds ");
	          try {
	            Thread.sleep(TimeUnit.SECONDS.toMillis(sleepTime));
	          } catch (Exception se) {
	            logger.warning( "Exception while sleeping" + se.toString());
	          }
	        }
	      }
	    }
	    logger.info("Tracing enabled - Stackdriver exporter initialized.");
	  }
	private static void initJaeger() {
	    String jaegerAddr = System.getenv("JAEGER_SERVICE_ADDR");
	    if (jaegerAddr != null && !jaegerAddr.isEmpty()) {
	      String jaegerUrl = String.format("http://%s/api/traces", jaegerAddr);
	
	      JaegerTraceExporter.createAndRegister(
	          JaegerExporterConfiguration.builder()
	              .setThriftEndpoint(jaegerUrl)
	              .setServiceName("ratingservice")
	              .build());
	      logger.info("Jaeger initialization complete.");
	    } else {
	      logger.info("Jaeger initialization disabled.");
	    }
	  }

    private static void setupOpenCensusAndExporters() throws IOException {

        RpcViews.registerAllViews();        
        new Thread(
                () -> {
                  initStats();
                  initTracing();
                })
            .start();
        initJaeger();
        
        
    }
	
	

	
	

	
	
}