<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="org.techmart.lk.core.entity.Product" %>
<%@ page import="org.techmart.lk.core.entity.Warehouse" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>TechMart - Product & Inventory Management</title>

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
            grid-template-columns: 2fr 1fr;
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
            background: linear-gradient(135deg, var(--accent-color), #8a63e5);
            border: none;
            border-radius: 6px;
            color: white;
            font-size: 0.95rem;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s ease;
            text-align: center;
        }

        .btn-action:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 15px rgba(88, 166, 255, 0.3);
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

        .badge-colombo { background: rgba(56, 139, 253, 0.15); color: #58a6ff; border: 1px solid rgba(56, 139, 253, 0.3); }
        .badge-kandy { background: rgba(188, 140, 255, 0.15); color: #d3b6ff; border: 1px solid rgba(188, 140, 255, 0.3); }
        .badge-galle { background: rgba(46, 160, 67, 0.15); color: #3fb950; border: 1px solid rgba(46, 160, 67, 0.3); }
        .badge-unknown { background: rgba(139, 148, 158, 0.15); color: #8b949e; border: 1px solid rgba(139, 148, 158, 0.3); }

        .btn-edit, .btn-delete {
            background: none;
            border: none;
            color: var(--accent-color);
            cursor: pointer;
            font-weight: 500;
            margin-right: 12px;
            font-size: 0.9rem;
            transition: color 0.2s;
        }

        .btn-delete {
            color: #ff7b72;
        }

        .btn-edit:hover { color: #82baff; text-decoration: underline; }
        .btn-delete:hover { color: #ff9b94; text-decoration: underline; }

        .stock-indicator {
            font-weight: 600;
        }
        .stock-critical { color: #f85149; }
        .stock-low { color: #e3b341; }
        .stock-good { color: #56d364; }

    </style>
</head>
<body>

    <div class="sidebar">
        <h2>Tech<span>Mart</span></h2>
        <ul class="nav-menu">
            <li class="nav-item active"><a href="<%= request.getContextPath() %>/products">Inventory Log</a></li>
            <li class="nav-item"><a href="<%= request.getContextPath() %>/orders">Order Queue</a></li>
            <li class="nav-item"><a href="<%= request.getContextPath() %>/dashboard">Metrics Hub</a></li>
        </ul>

        <div class="sidebar-footer">
            <p>Admin: <%= session.getAttribute("username") %></p>
            <a href="<%= request.getContextPath() %>/login?action=logout" class="btn-logout">Terminate Session</a>
        </div>
    </div>

    <div class="main-content">
        <div class="header">
            <h1>Product & Inventory Management</h1>
        </div>

        <div class="grid-container">

            <div class="glass-card">
                <div class="card-title">Active Warehouse Catalog</div>
                <table>
                    <thead>
                        <tr>
                            <th>Item ID</th>
                            <th>Name</th>
                            <th>Description</th>
                            <th>Price</th>
                            <th>Quantity</th>
                            <th>Warehouse</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                            List<Product> products = (List<Product>) request.getAttribute("products");
                            List<Warehouse> warehouses = (List<Warehouse>) request.getAttribute("warehouses");
                            if (products != null && !products.isEmpty()) {
                                for (Product p : products) {
                                    String whName = "Unknown";
                                    String badgeClass = "badge-unknown";
                                    if (warehouses != null) {
                                        for (Warehouse w : warehouses) {
                                            if (w.getId().equals(p.getWarehouseId())) {
                                                whName = w.getName();
                                                if (whName.contains("Colombo")) badgeClass = "badge-colombo";
                                                else if (whName.contains("Kandy")) badgeClass = "badge-kandy";
                                                else if (whName.contains("Galle")) badgeClass = "badge-galle";
                                                break;
                                            }
                                        }
                                    }

                                    String stockClass = "stock-good";
                                    if (p.getQuantity() <= 10) stockClass = "stock-critical";
                                    else if (p.getQuantity() <= 50) stockClass = "stock-low";
                        %>
                                    <tr>
                                        <td>#<%= p.getId() %></td>
                                        <td style="color: var(--text-heading); font-weight: 500;"><%= p.getName() %></td>
                                        <td><%= p.getDescription() %></td>
                                        <td style="font-weight: 600;">Rs. <%= String.format("%,.2f", p.getPrice()) %></td>
                                        <td><span class="stock-indicator <%= stockClass %>"><%= p.getQuantity() %> Units</span></td>
                                        <td><span class="badge <%= badgeClass %>"><%= whName %></span></td>
                                        <td>
                                            <button class="btn-edit" onclick="editProduct(<%= p.getId() %>, '<%= p.getName().replace("'", "\\'") %>', '<%= p.getDescription().replace("'", "\\'") %>', <%= p.getPrice() %>, <%= p.getQuantity() %>, <%= p.getWarehouseId() %>)">Edit</button>
                                            <form action="<%= request.getContextPath() %>/products" method="post" style="display:inline;" onsubmit="return confirm('Are you sure you want to delete this product?');">
                                                <input type="hidden" name="action" value="delete">
                                                <input type="hidden" name="id" value="<%= p.getId() %>">
                                                <button type="submit" class="btn-delete">Delete</button>
                                            </form>
                                        </td>
                                    </tr>
                        <%
                                }
                            } else {
                        %>
                                <tr>
                                    <td colspan="7" style="text-align: center; color: #8b949e; padding: 30px;">No products available. Seeding database...</td>
                                </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>

            <div class="glass-card" style="height: fit-content;">
                <div class="card-title" id="form-title">Provision Product</div>
                <form id="product-form" action="<%= request.getContextPath() %>/products" method="post">
                    <input type="hidden" name="action" id="form-action" value="add">
                    <input type="hidden" name="id" id="product-id" value="">

                    <div class="form-group">
                        <label for="name">Product Name</label>
                        <input type="text" id="name" name="name" class="form-control" placeholder="e.g. ThinkPad Laptop" required autocomplete="off">
                    </div>

                    <div class="form-group">
                        <label for="description">Item Details</label>
                        <textarea id="description" name="description" class="form-control" rows="3" placeholder="Brief technical specifications..." required></textarea>
                    </div>

                    <div class="form-group">
                        <label for="price">Retail Price (Rs.)</label>
                        <input type="number" id="price" name="price" class="form-control" step="0.01" min="0" placeholder="0.00" required>
                    </div>

                    <div class="form-group">
                        <label for="quantity">Initial Stock Count</label>
                        <input type="number" id="quantity" name="quantity" class="form-control" min="0" placeholder="Quantity" required>
                    </div>

                    <div class="form-group">
                        <label for="warehouseId">Assigned Warehouse</label>
                        <select id="warehouseId" name="warehouseId" class="form-control" required>
                            <%
                                if (warehouses != null) {
                                    for (Warehouse w : warehouses) {
                            %>
                                        <option value="<%= w.getId() %>"><%= w.getName() %> (<%= w.getLocation() %>)</option>
                            <%
                                    }
                                }
                            %>
                        </select>
                    </div>

                    <button type="submit" class="btn-action" id="btn-submit">Submit Record</button>
                    <button type="button" class="btn-action" id="btn-cancel" style="background: rgba(139, 148, 158, 0.15); color: var(--text-color); margin-top: 10px; display: none;" onclick="resetForm()">Cancel Update</button>
                </form>
            </div>
        </div>
    </div>

    <script>
        function editProduct(id, name, desc, price, qty, whId) {
            document.getElementById('form-title').innerText = 'Modify Product #' + id;
            document.getElementById('form-action').value = 'update';
            document.getElementById('product-id').value = id;
            document.getElementById('name').value = name;
            document.getElementById('description').value = desc;
            document.getElementById('price').value = price;
            document.getElementById('quantity').value = qty;
            document.getElementById('warehouseId').value = whId;
            document.getElementById('btn-submit').innerText = 'Apply Changes';
            document.getElementById('btn-cancel').style.display = 'block';
            window.scrollTo({ top: 0, behavior: 'smooth' });
        }

        function resetForm() {
            document.getElementById('form-title').innerText = 'Provision Product';
            document.getElementById('form-action').value = 'add';
            document.getElementById('product-id').value = '';
            document.getElementById('product-form').reset();
            document.getElementById('btn-submit').innerText = 'Submit Record';
            document.getElementById('btn-cancel').style.display = 'none';
        }
    </script>
</body>
</html>
