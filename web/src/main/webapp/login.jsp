<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>TechMart Admin - Login</title>

    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;600;800&display=swap" rel="stylesheet">
    <style>
        :root {
            --bg-color: #0d1117;
            --accent-color: #58a6ff;
            --accent-glow: rgba(88, 166, 255, 0.4);
            --card-bg: rgba(22, 27, 34, 0.8);
            --border-color: rgba(240, 246, 252, 0.1);
            --text-color: #c9d1d9;
            --text-heading: #f0f6fc;
        }

        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
            font-family: 'Outfit', sans-serif;
        }

        body {
            background-color: var(--bg-color);
            background-image: radial-gradient(circle at 10% 20%, rgba(90, 120, 250, 0.05) 0%, transparent 40%),
                              radial-gradient(circle at 90% 80%, rgba(200, 80, 250, 0.05) 0%, transparent 40%);
            min-height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
            color: var(--text-color);
            overflow: hidden;
        }

        .login-container {
            width: 100%;
            max-width: 420px;
            padding: 40px;
            background: var(--card-bg);
            border: 1px solid var(--border-color);
            border-radius: 16px;
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5), 0 0 1px 1px var(--border-color);
            backdrop-filter: blur(12px);
            position: relative;
            animation: fadeIn 0.8s ease-out;
        }

        .login-container::before {
            content: '';
            position: absolute;
            top: -2px;
            left: -2px;
            right: -2px;
            bottom: -2px;
            background: linear-gradient(135deg, var(--accent-color), #bc8cff, transparent 40%);
            z-index: -1;
            border-radius: 18px;
            opacity: 0.3;
        }

        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(20px); }
            to { opacity: 1; transform: translateY(0); }
        }

        .logo-section {
            text-align: center;
            margin-bottom: 30px;
        }

        .logo-section h1 {
            font-size: 2.5rem;
            font-weight: 800;
            color: var(--text-heading);
            letter-spacing: -0.5px;
            margin-bottom: 5px;
        }

        .logo-section h1 span {
            background: linear-gradient(135deg, var(--accent-color), #bc8cff);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
        }

        .logo-section p {
            font-size: 0.9rem;
            color: #8b949e;
        }

        .form-group {
            margin-bottom: 20px;
            position: relative;
        }

        .form-group label {
            display: block;
            font-size: 0.85rem;
            font-weight: 600;
            color: #8b949e;
            margin-bottom: 8px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        .form-control {
            width: 100%;
            padding: 12px 16px;
            background: rgba(1, 4, 9, 0.5);
            border: 1px solid var(--border-color);
            border-radius: 8px;
            color: var(--text-heading);
            font-size: 1rem;
            transition: all 0.3s ease;
        }

        .form-control:focus {
            outline: none;
            border-color: var(--accent-color);
            box-shadow: 0 0 0 3px var(--accent-glow);
            background: rgba(1, 4, 9, 0.8);
        }

        .btn-submit {
            width: 100%;
            padding: 14px;
            background: linear-gradient(135deg, var(--accent-color), #8a63e5);
            border: none;
            border-radius: 8px;
            color: white;
            font-size: 1rem;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s ease;
            box-shadow: 0 4px 15px rgba(88, 166, 255, 0.2);
            margin-top: 10px;
        }

        .btn-submit:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(88, 166, 255, 0.4);
            filter: brightness(1.1);
        }

        .btn-submit:active {
            transform: translateY(0);
        }

        .error-message {
            background: rgba(248, 81, 73, 0.15);
            border: 1px solid rgba(248, 81, 73, 0.3);
            color: #ff7b72;
            padding: 12px;
            border-radius: 8px;
            font-size: 0.9rem;
            margin-bottom: 20px;
            text-align: center;
        }
    </style>
</head>
<body>

    <div class="login-container">
        <div class="logo-section">
            <h1>Tech<span>Mart</span></h1>
            <p>Enterprise Operations Modernization</p>
        </div>

        <% if (request.getAttribute("error") != null) { %>
            <div class="error-message">
                <%= request.getAttribute("error") %>
            </div>
        <% } %>

        <form action="<%= request.getContextPath() %>/login" method="post">
            <div class="form-group">
                <label for="username">Administrator ID</label>
                <input type="text" id="username" name="username" class="form-control" placeholder="e.g. admin" required autocomplete="off">
            </div>

            <div class="form-group">
                <label for="password">Security Password</label>
                <input type="password" id="password" name="password" class="form-control" placeholder="••••••••" required>
            </div>

            <button type="submit" class="btn-submit">Authenticate Session</button>
        </form>
    </div>

</body>
</html>
