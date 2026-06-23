<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="org.techmart.lk.core.entity.Order" %>
<%@ page import="org.techmart.lk.core.entity.Product" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>TechMart - Order Processing System</title>
    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <style>
        :root {
            --bg-color: #0d1117;
            --accent-color: #58a6ff;
            --accent-glow: rgba(88, 166, 255, 0.25);
            --card-bg: rgba(22, 27, 34, 0.6);
            --border-color: rgba(240, 246, 252, 0.1);
            --text-color: #c9d1d9;
            --text-heading: #f0f6fc;
            --sidebar-width: 250px;
        }

        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
            font-family: 'Outfit', sans-serif;
        }

        body {
            background-color: var(--bg-color);
            background-image: radial-gradient(circle at 10% 20%, rgba(90, 120, 250, 0.03) 0%, transparent 40%),
                              radial-gradient(circle at 90% 80%, rgba(200, 80, 250, 0.03) 0%, transparent 40%);
            min-height: 100vh;
            color: var(--text-color);
            display: flex;
        }

        /* Sidebar Navigation */
        .sidebar {
            width: var(--sidebar-width);
            background: rgba(22, 27, 34, 0.85);
            border-right: 1px solid var(--border-color);
            backdrop-filter: blur(10px);
            padding: 30px 20px;
            display: flex;
            flex-direction: column;
            position: fixed;
            height: 100vh;
            z-index: 10;
        }

        .sidebar h2 {
            font-size: 1.8rem;
            font-weight: 800;
            color: var(--text-heading);
            margin-bottom: 40px;
            text-align: center;
        }

        .sidebar h2 span {
            background: linear-gradient(135deg, var(--accent-color), #bc8cff);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
        }

        .nav-menu {
            list-style: none;
            display: flex;
            flex-direction: column;
            gap: 10px;
        }

        .nav-item a {
            display: flex;
            align-items: center;
            padding: 12px 16px;
            color: var(--text-color);
            text-decoration: none;
            border-radius: 8px;
            font-size: 0.95rem;
            font-weight: 500;
            transition: all 0.3s ease;
        }

        .nav-item.active a, .nav-item a:hover {
            background: rgba(88, 166, 255, 0.1);
            color: var(--accent-color);
            transform: translateX(5px);
        }

        .sidebar-footer {
            margin-top: auto;
            border-top: 1px solid var(--border-color);
            padding-top: 20px;
            text-align: center;
        }

        .sidebar-footer p {
            font-size: 0.8rem;
            color: #8b949e;
            margin-bottom: 10px;
        }

        .btn-logout {
            display: inline-block;
            width: 100%;
            padding: 10px;
            background: rgba(248, 81, 73, 0.15);
            border: 1px solid rgba(248, 81, 73, 0.3);
            border-radius: 8px;
            color: #ff7b72;
            text-decoration: none;
            font-size: 0.9rem;
            font-weight: 600;
            transition: all 0.3s ease;
        }

        .btn-logout:hover {
            background: #ff7b72;
            color: white;
            box-shadow: 0 4px 15px rgba(248, 81, 73, 0.3);
        }

        /* Main Content */
        .main-content {
            margin-left: var(--sidebar-width);
            flex: 1;
            padding: 40px;
            max-width: 1400px;
        }

        .header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 30px;
        }

        .header h1 {
            font-size: 2rem;
            font-weight: 700;
            color: var(--text-heading);
        }

        .grid-container {
            display: grid;
            grid-template-columns: 1fr 2fr;
            gap: 30px;
        }

        /* Cards and Elements */
        .glass-card {
            background: var(--card-bg);
            border: 1px solid var(--border-color);
            border-radius: 12px;
            padding: 24px;
            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
            backdrop-filter: blur(8px);
        }

        .card-title {
            font-size: 1.25rem;
            font-weight: 600;
            color: var(--text-heading);
            margin-bottom: 20px;
            border-bottom: 1px solid var(--border-color);
            padding-bottom: 10px;
        }

        /* Form styling */
        .form-group {
            margin-bottom: 18px;
        }

        .form-group label {
            display: block;
            font-size: 0.85rem;
            font-weight: 600;
            color: #8b949e;
            margin-bottom: 8px;
        }

        .form-control {
            width: 100%;
            padding: 10px 14px;
            background: rgba(1, 4, 9, 0.4);
            border: 1px solid var(--border-color);
            border-radius: 6px;
            color: var(--text-heading);
            font-size: 0.95rem;
            transition: all 0.3s ease;
        }

        .form-control:focus {
            outline: none;
            border-color: var(--accent-color);
            box-shadow: 0 0 0 3px var(--accent-glow);
        }

        .btn-action {
            display: inline-block;
            width: 100%;
            padding: 12px;
            background: linear-gradient(135deg, #2ea44f, #238636);
            border: none;
            border-radius: 6px;
            color: white;
            font-size: 0.95rem;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s ease;
            text-align: center;
            box-shadow: 0 4px 12px rgba(46, 160, 67, 0.2);
        }

        .btn-action:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 15px rgba(46, 160, 67, 0.4);
            filter: brightness(1.1);
        }

        /* Message banners */
        .banner {
            padding: 14px 18px;
            border-radius: 8px;
            margin-bottom: 25px;
            font-size: 0.95rem;
            display: flex;
            align-items: center;
            border: 1px solid transparent;
        }

        .banner-success {
            background: rgba(46, 160, 67, 0.15);
            border-color: rgba(46, 160, 67, 0.3);
            color: #56d364;
        }

        .banner-error {
            background: rgba(248, 81, 73, 0.15);
            border-color: rgba(248, 81, 73, 0.3);
            color: #ff7b72;
        }

        /* Table design */
        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 10px;
        }

        th {
            text-align: left;
            padding: 14px 16px;
            border-bottom: 2px solid var(--border-color);
            color: #8b949e;
            font-size: 0.85rem;
            font-weight: 600;
            text-transform: uppercase;
        }

        td {
            padding: 16px;
            border-bottom: 1px solid var(--border-color);
            font-size: 0.95rem;
        }

        tr:hover td {
            background: rgba(255, 255, 255, 0.02);
        }

        .badge {
            display: inline-block;
            padding: 4px 8px;
            border-radius: 20px;
            font-size: 0.75rem;
            font-weight: 600;
        }

        .badge-pending { background: rgba(240, 139, 0, 0.15); color: #f08b00; border: 1px solid rgba(240, 139, 0, 0.3); }
        .badge-processed { background: rgba(46, 160, 67, 0.15); color: #3fb950; border: 1px solid rgba(46, 160, 67, 0.3); }

    </style>
</head>
<body>

    <!-- Sidebar Navigation -->
    <div class="sidebar">
        <h2>Tech<span>Mart</span></h2>
        <ul class="nav-menu">
            <li class="nav-item"><a href="<%= request.getContextPath() %>/products">Inventory Log</a></li>
            <li class="nav-item active"><a href="<%= request.getContextPath() %>/orders">Order Queue</a></li>
            <li class="nav-item"><a href="<%= request.getContextPath() %>/dashboard">Metrics Hub</a></li>
        </ul>

        <div class="sidebar-footer">
            <p>Admin: <%= session.getAttribute("username") %></p>
            <a href="<%= request.getContextPath() %>/login?action=logout" class="btn-logout">Terminate Session</a>
        </div>
    </div>

    <!-- Main Content -->
    <div class="main-content">
        <div class="header">
            <h1>Automated Order Processing</h1>
        </div>

        <!-- Feedback banners -->
        <% if (session.getAttribute("successMessage") != null) { %>
            <div class="banner banner-success">
                <%= session.getAttribute("successMessage") %>
            </div>
            <% session.removeAttribute("successMessage"); %>
        <% } %>
        <% if (session.getAttribute("errorMessage") != null) { %>
            <div class="banner banner-error">
                <%= session.getAttribute("errorMessage") %>
            </div>
            <% session.removeAttribute("errorMessage"); %>
        <% } %>

        <div class="grid-container">
            <!-- Checkout Processing Form -->
            <div class="glass-card" style="height: fit-content;">
                <div class="card-title">Initiate Checkout</div>
                <form action="<%= request.getContextPath() %>/orders" method="post">
                    <input type="hidden" name="action" value="placeOrder">

                    <div class="form-group">
                        <label for="customerName">Customer Identification</label>
                        <input type="text" id="customerName" name="customerName" class="form-control" placeholder="e.g. John Doe (or 'error' for failure recovery testing)" required autocomplete="off">
                    </div>

                    <div class="form-group">
                        <label for="productId">Select Product</label>
                        <select id="productId" name="productId" class="form-control" required>
                            <% 
                                List<Product> products = (List<Product>) request.getAttribute("products");
                                if (products != null) {
                                    for (Product p : products) {
                            %>
                                        <option value="<%= p.getId() %>"><%= p.getName() %> (Available: <%= p.getQuantity() %>) - Rs. <%= String.format("%,.2f", p.getPrice()) %></option>
                            <% 
                                    }
                                } 
                            %>
                        </select>
                    </div>

                    <div class="form-group">
                        <label for="quantity">Order Quantity</label>
                        <input type="number" id="quantity" name="quantity" class="form-control" min="1" value="1" required>
                    </div>

                    <button type="submit" class="btn-action">Execute Checkout</button>
                </form>
            </div>

            <!-- Orders Log -->
            <div class="glass-card">
                <div class="card-title">Order Processing Register</div>
                <table>
                    <thead>
                        <tr>
                            <th>Order ID</th>
                            <th>Date & Time</th>
                            <th>Customer Account</th>
                            <th>Total Purchase</th>
                            <th>Status</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% 
                            List<Order> orders = (List<Order>) request.getAttribute("orders");
                            if (orders != null && !orders.isEmpty()) {
                                for (Order o : orders) {
                                    String statusBadge = "badge-pending";
                                    // MDB automatically updates or logs. We default display processed for simplicity or log order status.
                                    // We keep it as PENDING and in MDB we just process the notification. Order starts as PENDING.
                                    if ("PROCESSED".equals(o.getStatus())) {
                                        statusBadge = "badge-processed";
                                    }
                        %>
                                    <tr>
                                        <td>#<%= o.getId() %></td>
                                        <td><%= o.getOrderDate().toString() %></td>
                                        <td style="color: var(--text-heading); font-weight: 500;"><%= o.getCustomerName() %></td>
                                        <td style="font-weight: 600; color: #56d364;">Rs. <%= String.format("%,.2f", o.getTotalAmount()) %></td>
                                        <td><span class="badge <%= statusBadge %>"><%= o.getStatus() %></span></td>
                                    </tr>
                        <% 
                                }
                            } else { 
                        %>
                                <tr>
                                    <td colspan="5" style="text-align: center; color: #8b949e; padding: 30px;">No checkout records found.</td>
                                </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

</body>
</html>
