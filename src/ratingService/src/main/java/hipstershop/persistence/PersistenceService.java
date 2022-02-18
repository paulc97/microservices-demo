package hipstershop.persistence;

import java.sql.*;

public class PersistenceService {
	String url = "jdbc:postgresql://34.141.53.87/postgres";
	String user = "postgres";
	String password = "78en8hLiwpPuJOdB";

	public void saveProductRating(String productId, Integer rating) {
		if (productId == null || rating == null) return;
		if (rating > 5) rating = 5;
		if (rating < 1) rating = 1;
		try (Connection connection = DriverManager.getConnection(url, user, password)) {
			Statement statement = connection.createStatement();
			statement.executeUpdate("INSERT INTO product_ratings(product_id, rating) VALUES('" + productId + "', " + rating + ");");
		}
		catch (SQLException e) {
			System.out.println("Connection failed");
			e.printStackTrace();
		}

	}

	public double getProductRating(String productId) {
		if (productId == null) return 0;
		try (Connection connection = DriverManager.getConnection(url, user, password)) {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery("SELECT avg(rating) FROM product_ratings WHERE product_id='" + productId +"';");
			//add zeros to 1d in order to get more decimal places (e.g. 100d for two decimal places)
			if(resultSet.next()) {return (double)Math.round(resultSet.getDouble(1) * 1d) / 1d;}
		}
		catch (SQLException e) {
			System.out.println("Connection failed");
			e.printStackTrace();
		}
		return 0;
	}

	public int getNumberOfProductRatings(String productId) {
		if (productId == null) return 0;
		try (Connection connection = DriverManager.getConnection(url, user, password)) {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery("SELECT count(rating) FROM product_ratings WHERE product_id='" + productId +"';");
			if(resultSet.next()) {return resultSet.getInt(1);}
		}
		catch (SQLException e) {
			System.out.println("Connection failed");
			e.printStackTrace();
		}
		return 0;
	}

	public void saveShopRating(Integer rating) {
		if (rating == null) return;
		if (rating > 5) rating = 5;
		if (rating < 1) rating = 1;
		try (Connection connection = DriverManager.getConnection(url, user, password)) {
			Statement statement = connection.createStatement();
			statement.executeUpdate("INSERT INTO shop_ratings(rating) VALUES(" + rating + ");");
		}
		catch (SQLException e) {
			System.out.println("Connection failed");
			e.printStackTrace();
		}

	}

	public double getShopRating() {
		try (Connection connection = DriverManager.getConnection(url, user, password)) {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery("SELECT avg(rating) FROM shop_ratings;");
			//add zeros to 1d in order to get more decimal places (e.g. 100d for two decimal places)
			if(resultSet.next()) {return (double)Math.round(resultSet.getDouble(1) * 1d) / 1d;}
		}
		catch (SQLException e) {
			System.out.println("Connection failed");
			e.printStackTrace();
		}
		return 0;
	}

	public int getNumberOfShopRatings() {
		try (Connection connection = DriverManager.getConnection(url, user, password)) {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery("SELECT count(rating) FROM shop_ratings;");
			if(resultSet.next()) {return resultSet.getInt(1);}
		}
		catch (SQLException e) {
			System.out.println("Connection failed");
			e.printStackTrace();
		}
		return 0;
	}

	
}
