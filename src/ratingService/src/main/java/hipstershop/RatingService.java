package hipstershop;

import hipstershop.persistence.PersistenceService;
import io.grpc.stub.StreamObserver;
import static java.lang.Math.toIntExact;

public class RatingService extends RatingServiceGrpc.RatingServiceImplBase {
	
	private hipstershop.persistence.PersistenceService persistenceService = new PersistenceService();
	
	@Override
	public void rateProduct(ProductRequest request, StreamObserver<ApiResponse> responseObserver) {
		
		persistenceService.saveProductRating(request.getProductId(), toIntExact(request.getRating()));
		ApiResponse.Builder response = ApiResponse.newBuilder();
		
		response.setResponseMessage("SUCCESS: product rating saved");
		responseObserver.onNext(response.build());
		responseObserver.onCompleted();
	}

	@Override
	public void getProductRating(ProductRatingRequest request, StreamObserver<ProductRatingResponse> responseObserver) {
		
		ProductRatingResponse.Builder response = ProductRatingResponse.newBuilder();
		
		response.setRating(persistenceService.getProductRating(request.getProductId()));
		response.setRatingCount(Long.valueOf(persistenceService.getNumberOfProductRatings(request.getProductId())));
		response.setResponseMessage("SUCESS: rating send");
		responseObserver.onNext(response.build());
		responseObserver.onCompleted();
	}

	@Override
	public void rateShop(ShopRequest request, StreamObserver<ApiResponse> responseObserver) {
		
		persistenceService.saveShopRating(toIntExact(request.getRating()));
		ApiResponse.Builder response = ApiResponse.newBuilder();
		
		response.setResponseMessage("SUCCESS: shop rating saved");
		responseObserver.onNext(response.build());
		responseObserver.onCompleted();
	}

	@Override
	public void getShopRating(Empty request, StreamObserver<ShopRatingResponse> responseObserver) {
		
		ShopRatingResponse.Builder response = ShopRatingResponse.newBuilder();
		
		response.setRating(persistenceService.getShopRating());
		response.setRatingCount(Long.valueOf(persistenceService.getNumberOfShopRatings()));
		response.setResponseMessage("SUCESS: rating send");
		responseObserver.onNext(response.build());
		responseObserver.onCompleted();
	}
	
}
