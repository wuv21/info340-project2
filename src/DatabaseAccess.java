import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseAccess {

	public static Order[] GetPendingOrders() {
		String url = "jdbc:sqlserver://is-fleming.ischool.uw.edu";
		String user = "perry";
		String pass = "Info340C";

		try {
			Connection conn = DriverManager.getConnection(url, user, pass);
			conn.setCatalog("Store");

			String query = "SELECT COUNT(*) AS co FROM Orders " + "WHERE status='Pending'";
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);

			rs.next();
			int size = rs.getInt("co");

			Order[] orders = new Order[size];

			query = "select a.OrderID, a.OrderDate, a.BillingAddress, a.BillingAddress, a.BillingInfo, a.ShippingAddress, "
					+ "a.Status, b.CustomerID, b.FirstName, b.LastName, b.Email, c.PricePaid FROM Orders a "
					+ "JOIN Customer b ON a.CustomerID = b.CustomerID "
					+ "LEFT JOIN LineItems c ON c.OrderID = a.OrderID " + "WHERE status='Pending' "
					+ "ORDER BY a.orderID";
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);

			int i = -1;
			int prevOrderID = -1;
			while (rs.next()) {
				int currentOrderID = rs.getInt("OrderID");

				if (currentOrderID != prevOrderID) {
					Order o = new Order();
					o.OrderID = rs.getInt("OrderID");
					o.OrderDate = rs.getDate("OrderDate");
					o.BillingAddress = rs.getString("BillingAddress");
					o.BillingInfo = rs.getString("BillingInfo");
					o.ShippingAddress = rs.getString("ShippingAddress");
					o.Status = rs.getString("Status");
					o.Customer = new Customer();
					o.Customer.CustomerID = rs.getInt("CustomerID");
					o.Customer.Name = rs.getString("FirstName") + " " + rs.getString("LastName");
					o.Customer.Email = rs.getString("Email");
					o.TotalCost = rs.getInt("PricePaid");

					i++;
					orders[i] = o;
					prevOrderID = currentOrderID;

				} else {
					orders[i].TotalCost += rs.getInt("PricePaid");
				}
			}

			return orders;

		} catch (Exception e) {
			System.out.println("error");
			e.printStackTrace();
		}

		return new Order[] {};
	}

	public static Product[] GetProducts() {
		String url = "jdbc:sqlserver://is-fleming.ischool.uw.edu";
		String user = "perry";
		String pass = "Info340C";

		int size = 0;
		try {
			Connection conn = DriverManager.getConnection(url, user, pass);
			conn.setCatalog("Store");

			String sizeQuery = "SELECT COUNT(*) as total FROM Products";
			Statement stmt1 = conn.createStatement();
			ResultSet rs1 = stmt1.executeQuery(sizeQuery);

			rs1.next();
			size = rs1.getInt("total");

		} catch (Exception e) {
			System.out.println("error");
			e.printStackTrace();
		}

		Product[] results = new Product[size];
		try {
			Connection conn = DriverManager.getConnection(url, user, pass);
			conn.setCatalog("Store");

			String dataQuery = "SELECT * FROM Products";
			Statement stmt2 = conn.createStatement();
			ResultSet rs2 = stmt2.executeQuery(dataQuery);

			int i = 0;
			while (rs2.next()) {
				Product p = new Product();
				p.Description = rs2.getString("Description");
				p.Name = rs2.getString("Name");
				p.InStock = rs2.getInt("QuantityOnHand");
				p.Price = rs2.getDouble("Cost");
				p.ProductID = rs2.getInt("ItemID");
				results[i] = p;
				i++;
			}
		} catch (Exception e) {
			System.out.println("error");
			e.printStackTrace();
		}
		return results;
	}

	public static Order GetOrderDetails(int OrderID) {

		String url = "jdbc:sqlserver://is-fleming.ischool.uw.edu";
		String user = "perry";
		String pass = "Info340C";

		try {
			Connection conn = DriverManager.getConnection(url, user, pass);
			conn.setCatalog("Store");

			String query = "Select * FROM Orders WHERE OrderID = " + OrderID;
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			rs.next();
			int custID = rs.getInt("CustomerId");

			String query3 = "Select * FROM Customer WHERE CustomerID = " + custID;
			Statement stmt3 = conn.createStatement();
			ResultSet rs3 = stmt3.executeQuery(query3);
			rs3.next();

			Order o = new Order();
			o.OrderID = OrderID;
			o.Customer = new Customer();
			o.Customer.Name = rs3.getString("FirstName") + " " + rs3.getString("LastName");
			o.Customer.Email = rs3.getString("Email");
			o.OrderDate = rs.getDate("OrderDate");
			o.Status = rs.getString("Status");
			o.BillingAddress = rs.getString("BillingAddress");
			o.BillingInfo = rs.getString("BillingInfo");
			o.ShippingAddress = rs.getString("ShippingAddress");

			String query2 = "SELECT count(*) AS total FROM LineItems WHERE OrderID = " + OrderID;
			Statement stmt2 = conn.createStatement();
			ResultSet rs2 = stmt2.executeQuery(query2);
			rs2.next();
			int size = rs2.getInt("total");

			LineItem[] lineItems = new LineItem[size];

			double totalPaid = 0;
			int i = 0;

			String query4 = "SELECT * FROM LineItems WHERE OrderID = " + OrderID;
			Statement stmt4 = conn.createStatement();
			ResultSet rs4 = stmt4.executeQuery(query4);

			while (rs4.next()) {
				LineItem li = new LineItem();
				li.Order = o;
				li.PricePaid = rs4.getDouble("PricePaid");
				li.Product = new Product();
				int productID = rs4.getInt("ProductID");
				li.Product.ProductID = productID;

				String query5 = "SELECT * FROM Products WHERE ItemID = " + productID;
				Statement stmt5 = conn.createStatement();
				ResultSet rs5 = stmt5.executeQuery(query5);
				rs5.next();

				li.Product.Description = rs5.getString("Description");
				li.Product.Name = rs5.getString("Name");
				li.Quantity = rs4.getInt("Quantity");

				lineItems[i] = li;
				i++;
				totalPaid += li.PricePaid;

			}

			o.TotalCost = totalPaid;
			o.LineItems = lineItems;

			return o;
		} catch (Exception e) {
			System.out.println("error");
			e.printStackTrace();
		}
		return new Order();
	}

	public static Product GetProductDetails(int ProductID) {
		String url = "jdbc:sqlserver://is-fleming.ischool.uw.edu";
		String user = "perry";
		String pass = "Info340C";

		Product p = new Product();

		try {
			Connection conn = DriverManager.getConnection(url, user, pass);
			conn.setCatalog("Store");

			String query = "SELECT COUNT(*) AS c FROM Products p " + "LEFT JOIN Comments c ON p.ItemID = c.ProductId "
					+ "WHERE ItemID = " + ProductID;
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);

			rs.next();
			int size = rs.getInt("c");
			String[] comments = new String[size];

			query = "SELECT * FROM Products p " + "LEFT JOIN Comments c ON p.ItemID = c.ProductId " + "WHERE ItemID = "
					+ ProductID;
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);

			int i = 0;
			while (rs.next()) {
				if (i == 0) {
					p.Description = rs.getString("Description");
					p.Name = rs.getString("Name");
					p.InStock = rs.getInt("QuantityOnHand");
					p.Price = rs.getDouble("Cost");
					p.ProductID = ProductID;
					p.UserComments = comments;
				}
				comments[i] = rs.getString("CommentText");
				i++;
			}
		} catch (Exception e) {
			System.out.println("error");
			e.printStackTrace();
		}
		return p;
	}

	public static Customer[] GetCustomers() {
		String url = "jdbc:sqlserver://is-fleming.ischool.uw.edu";
		String user = "perry";
		String pass = "Info340C";

		int size = 0;
		try {
			Connection conn = DriverManager.getConnection(url, user, pass);
			conn.setCatalog("Store");

			String query = "SELECT COUNT(*) as total FROM Customer";
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);

			rs.next();
			size = rs.getInt("total");

		} catch (Exception e) {
			System.out.println("error");
			e.printStackTrace();
		}

		Customer[] results = new Customer[size];
		try {
			Connection conn = DriverManager.getConnection(url, user, pass);
			conn.setCatalog("Store");

			String query = "SELECT * FROM Customer";
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);

			int i = 0;
			while (rs.next()) {
				Customer c = new Customer();
				c.CustomerID = rs.getInt("CustomerID");
				c.Email = rs.getString("Email");
				c.Name = rs.getString("FirstName") + " " + rs.getString("LastName");
				results[i] = c;
				i++;
			}
		} catch (Exception e) {
			System.out.println("error");
			e.printStackTrace();
		}
		return results;
	}

	public static Order[] GetCustomerOrders(Customer c) {
		String url = "jdbc:sqlserver://is-fleming.ischool.uw.edu";
		String user = "perry";
		String pass = "Info340C";

		int size = 0;
		try {
			Connection conn = DriverManager.getConnection(url, user, pass);
			conn.setCatalog("Store");

			String query = "SELECT COUNT(*) as total FROM Customer c " + "JOIN Orders o ON c.CustomerID = o.CustomerID "
					+ "JOIN LineItems li ON li.OrderID = o.OrderID " + "WHERE c.CustomerID LIKE " + c.CustomerID;
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);

			rs.next();
			size = rs.getInt("total");

		} catch (Exception e) {
			System.out.println("error");
			e.printStackTrace();
		}

		Order[] results = new Order[size];
		try {
			Connection conn = DriverManager.getConnection(url, user, pass);
			conn.setCatalog("Store");

			String query = "SELECT * FROM Customer c " + "JOIN Orders o ON c.CustomerID = o.CustomerID "
					+ "JOIN LineItems li ON li.OrderID = o.OrderID " + "WHERE c.CustomerID LIKE " + c.CustomerID;
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);

			int i = 0;
			while (rs.next()) {
				Order o = new Order();
				o.OrderID = rs.getInt("OrderID");
				o.Customer = new Customer();
				o.Customer.CustomerID = rs.getInt("CustomerID");
				o.Customer.Name = rs.getString("FirstName") + " " + rs.getString("LastName");
				o.Customer.Email = rs.getString("Email");
				o.OrderDate = rs.getDate("OrderDate");
				o.Status = rs.getString("Status");
				o.TotalCost = rs.getDouble("PricePaid");
				o.BillingAddress = rs.getString("BillingAddress");
				o.BillingInfo = rs.getString("BillingInfo");
				o.ShippingAddress = rs.getString("ShippingAddress");
				results[i] = o;
				i++;
			}
		} catch (Exception e) {
			System.out.println("error");
			e.printStackTrace();
		}
		return results;
	}

	public static Product[] SearchProductReviews(String query) {
		// DUMMY VALUES
		Product p = new Product();
		p.Description = "A great monitor";
		p.Name = "Monitor, 19 in";
		p.InStock = 10;
		p.Price = 196;
		p.ProductID = 1;
		p.Relavance = 0.7;
		return new Product[] { p };
	}

	public static void MakeOrder(Customer c, LineItem[] LineItems) {
		// TODO: Insert data into your database.
		// Show an error message if you can not make the reservation.
		String url = "jdbc:sqlserver://is-fleming.ischool.uw.edu";
		String user = "perry";
		String pass = "Info340C";
		
		int size = LineItems.length;
		PreparedStatement[] insertLineItems = new PreparedStatement[size];
		PreparedStatement[] updateProducts = new PreparedStatement[size];
		
		java.util.Date orderDate = null;
		String billingAddress = null;
		String billingInfo = null;
		String shippingAddress = null;

		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url, user, pass);
			conn.setCatalog("Store");

			conn.setAutoCommit(false);

			String query = "SELECT QuantityOnHand, Price FROM Products WHERE ItemID=?";
			PreparedStatement getQty = conn.prepareStatement(query);

			Map<Integer, Integer> items = new HashMap<Integer, Integer>();
			Map<Integer, Integer> quantities = new HashMap<Integer, Integer>();
			Map<Integer, Double> prices = new HashMap<Integer, Double>();
			
			boolean quantityCheck = true;
			for (LineItem item : LineItems) {
				getQty.setInt(1, item.Product.ProductID);
				ResultSet rs = getQty.executeQuery();
				
				int inStock = 0;
				double price = 0;
				if (rs.next()) {
					inStock = rs.getInt("QuantityOnHand");
					price = rs.getDouble("Price");
				}
				// Check quantity counts
				int diff = inStock - item.Quantity;
				if (diff < 0) {
					quantityCheck = false;
					break;
				} else {
					quantities.put(item.Product.ProductID, item.Quantity);
					items.put(item.Product.ProductID, inStock);
					prices.put(item.Product.ProductID, price);
				}
			}
			if (quantityCheck) {
				String orderQuery = ("INSERT INTO Orders(OrderDate, BillingAddress, BillingInfo, "
								+ "ShippingAddress, Status, CustomerID) " + "VALUES (?,?,?,?,?,?)");
				PreparedStatement insertOrder = conn
						.prepareStatement(query);
				insertOrder.setDate(1, new java.sql.Date(orderDate.getTime()));
				insertOrder.setString(2, billingAddress);
				insertOrder.setString(3, billingInfo);
				insertOrder.setString(4, shippingAddress);
				insertOrder.setString(5, "Ordered");
				insertOrder.setInt(6, c.CustomerID);

				int i = 0;
				int lastIndex = insertOrder.executeUpdate(orderQuery,Statement.RETURN_GENERATED_KEYS);
				for(Integer item : items.keySet()) {
					int orderID = lastIndex;
					orderDate = new Date();
					int productID = item.intValue();
					
					int quantity = quantities.get(productID);
					int onHand = items.get(productID);
					double price = prices.get(productID);
					
					PreparedStatement insertItem = conn.prepareStatement(
							"INSERT INTO LineItems(OrderID, ProductID, Quantity, PricePaid) " + "VALUES(?, ?, ?, ?)");
					insertItem.setInt(1, orderID);
					insertItem.setInt(2, productID);
					insertItem.setInt(3, quantity);
					insertItem.setDouble(4, quantity * price);
										
					insertLineItems[i] = insertItem;

					PreparedStatement updateProduct = conn.prepareStatement("UPDATE Store.Products "
							+ "SET Store.Products.QuantityOnHand=? " + "WHERE Store.Products.ItemID=?");
					updateProduct.setInt(1, onHand - quantity);
					updateProduct.setInt(2, productID);

					updateProducts[i] = updateProduct;
					i++;
				}
				for (PreparedStatement ps : insertLineItems) {
					ps.executeUpdate();
				}
				for (PreparedStatement ps : updateProducts) {
					ps.executeUpdate();
				}
				JOptionPane.showMessageDialog(null,
						"Created order for " + c.Name + " for " + Integer.toString(LineItems.length) + " items.");

			} else {
				JOptionPane.showMessageDialog(null,
						"Quantity exceeded for one or more items, please try again.");

			}

		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,
					"Transaction failed, please try again.");

			System.err.print("Transaction failed. Rollback in progress");
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}

	}
}