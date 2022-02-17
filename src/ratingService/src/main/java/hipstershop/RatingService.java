package hipstershop;

import hipstershop.*;
import hipstershop.grpc.RatingServiceGrpc;
import hipstershop.grpc.RatingServiceOuterClass;
import hipstershop.persistence.PersistenceService;
import io.grpc.stub.StreamObserver;
import static java.lang.Math.toIntExact;

public class RatingService extends RatingServiceGrpc.RatingServiceImplBase {
	
	private final hipstershop.persistence.PersistenceService persistenceService = new PersistenceService();
	
	@Override
	public void rateProduct(RatingServiceOuterClass.ProductRequest request, StreamObserver<RatingServiceOuterClass.ApiResponse> responseObserver) {
		
		persistenceService.saveProductRating(request.getProductId(), toIntExact(request.getRating()));
		RatingServiceOuterClass.ApiResponse.Builder response = RatingServiceOuterClass.ApiResponse.newBuilder();
		
		response.setResponseMessage("SUCCESS: product rating saved");
		responseObserver.onNext(response.build());
		responseObserver.onCompleted();
	}

	@Override
	public void getProductRating(RatingServiceOuterClass.ProductRatingRequest request, StreamObserver<RatingServiceOuterClass.ProductRatingResponse> responseObserver) {
		
		RatingServiceOuterClass.ProductRatingResponse.Builder response = RatingServiceOuterClass.ProductRatingResponse.newBuilder();
		
		response.setRating(persistenceService.getProductRating(request.getProductId()));
		response.setRatingCount(Long.valueOf(persistenceService.getNumberOfProductRatings(request.getProductId())));
		response.setResponseMessage("SUCESS: rating send");
		responseObserver.onNext(response.build());
		responseObserver.onCompleted();
	}

	@Override
	public void rateShop(RatingServiceOuterClass.ShopRequest request, StreamObserver<RatingServiceOuterClass.ApiResponse> responseObserver) {
		
		persistenceService.saveShopRating(toIntExact(request.getRating()));
		RatingServiceOuterClass.ApiResponse.Builder response = RatingServiceOuterClass.ApiResponse.newBuilder();
		
		response.setResponseMessage("SUCCESS: shop rating saved");
		responseObserver.onNext(response.build());
		responseObserver.onCompleted();
	}

	@Override
	public void getShopRating(RatingServiceOuterClass.Empty request, StreamObserver<RatingServiceOuterClass.ShopRatingResponse> responseObserver) {
		
		RatingServiceOuterClass.ShopRatingResponse.Builder response = RatingServiceOuterClass.ShopRatingResponse.newBuilder();
		
		response.setRating(persistenceService.getShopRating());
		response.setRatingCount(Long.valueOf(persistenceService.getNumberOfShopRatings()));
		response.setResponseMessage("SUCESS: rating send");
		responseObserver.onNext(response.build());
		responseObserver.onCompleted();
	}
	
}
