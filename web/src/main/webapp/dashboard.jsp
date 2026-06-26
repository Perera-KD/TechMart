<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="org.techmart.lk.ejb.bean.MetricsTrackerBean" %>
<%@ page import="org.techmart.lk.ejb.bean.InventoryCacheBean" %>
<%@ page import="org.techmart.lk.core.entity.AuditLog" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>TechMart - Monitoring Dashboard</title>

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

        .btn-reset {
            padding: 10px 18px;
            background: rgba(88, 166, 255, 0.1);
            border: 1px solid var(--accent-color);
            color: var(--accent-color);
            border-radius: 6px;
            cursor: pointer;
            font-size: 0.9rem;
            font-weight: 600;
            transition: all 0.3s ease;
        }

        .btn-reset:hover {
            background: var(--accent-color);
            color: var(--bg-color);
            box-shadow: 0 0 15px var(--accent-glow);
        }

        /* Grid layouts */
        .metrics-grid {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 20px;
            margin-bottom: 30px;
        }

        .glass-card {
            background: var(--card-bg);
            border: 1px solid var(--border-color);
            border-radius: 12px;
            padding: 24px;
            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
            backdrop-filter: blur(8px);
        }

        .metric-card {
            display: flex;
            flex-direction: column;
            position: relative;
            overflow: hidden;
        }

        .metric-card::after {
            content: '';
            position: absolute;
            bottom: 0;
            left: 0;
            width: 100%;
            height: 4px;
            background: linear-gradient(90deg, var(--accent-color), #bc8cff);
        }

        .metric-label {
            font-size: 0.85rem;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            color: #8b949e;
            margin-bottom: 10px;
            font-weight: 600;
        }

        .metric-value {
            font-size: 2rem;
            font-weight: 700;
            color: var(--text-heading);
            line-height: 1.2;
        }

        .metric-sub {
            font-size: 0.8rem;
            color: #8b949e;
            margin-top: 8px;
        }

        .dashboard-row {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 30px;
            margin-bottom: 30px;
        }

        .card-title {
            font-size: 1.25rem;
            font-weight: 600;
            color: var(--text-heading);
            margin-bottom: 20px;
            border-bottom: 1px solid var(--border-color);
            padding-bottom: 10px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .list-container {
            max-height: 300px;
            overflow-y: auto;
            display: flex;
            flex-direction: column;
            gap: 12px;
            padding-right: 5px;
        }

        /* Scrollbar styling */
        .list-container::-webkit-scrollbar {
            width: 6px;
        }
        .list-container::-webkit-scrollbar-track {
            background: rgba(255, 255, 255, 0.02);
        }
        .list-container::-webkit-scrollbar-thumb {
            background: var(--border-color);
            border-radius: 4px;
        }

        .log-item {
            background: rgba(1, 4, 9, 0.3);
            border: 1px solid var(--border-color);
            border-radius: 8px;
            padding: 12px 16px;
            font-size: 0.9rem;
            display: flex;
            flex-direction: column;
            gap: 5px;
        }

        .log-header {
            display: flex;
            justify-content: space-between;
            font-size: 0.75rem;
            font-weight: 600;
            color: #8b949e;
        }

        .log-type {
            color: var(--accent-color);
            text-transform: uppercase;
        }

        .log-msg {
            color: var(--text-heading);
        }

        /* Progress Bar */
        .progress-bar-container {
            background: rgba(255, 255, 255, 0.05);
            border-radius: 10px;
            height: 10px;
            overflow: hidden;
            margin-top: 15px;
        }

        .progress-bar {
            height: 100%;
            background: linear-gradient(90deg, var(--accent-color), #2ea44f);
            border-radius: 10px;
            transition: width 0.5s ease;
        }

    </style>
</head>
<body>

    <div class="sidebar">
        <h2>Tech<span>Mart</span></h2>
        <ul class="nav-menu">
            <li class="nav-item"><a href="<%= request.getContextPath() %>/products">Inventory Log</a></li>
            <li class="nav-item"><a href="<%= request.getContextPath() %>/orders">Order Queue</a></li>
            <li class="nav-item active"><a href="<%= request.getContextPath() %>/dashboard">Metrics Hub</a></li>
        </ul>

        <div class="sidebar-footer">
            <p>Admin: <%= session.getAttribute("username") %></p>
            <a href="<%= request.getContextPath() %>/login?action=logout" class="btn-logout">Terminate Session</a>
        </div>
    </div>

    <div class="main-content">
        <div class="header">
            <h1>System Performance & Audit Hub</h1>
            <form action="<%= request.getContextPath() %>/dashboard" method="get">
                <input type="hidden" name="reset" value="true">
                <button type="submit" class="btn-reset">Reset Statistics</button>
            </form>
        </div>

        <%
            MetricsTrackerBean metrics = (MetricsTrackerBean) request.getAttribute("metrics");
            InventoryCacheBean cache = (InventoryCacheBean) request.getAttribute("cache");
            List<AuditLog> dbLogs = (List<AuditLog>) request.getAttribute("dbLogs");
            List<String> sessionActions = (List<String>) request.getAttribute("sessionActions");

            long reqCount = metrics != null ? metrics.getHttpRequestCount() : 0;
            double avgResp = metrics != null ? metrics.getAverageResponseTime() : 0;
            long dbCount = metrics != null ? metrics.getDbQueryCount() : 0;
            double avgDb = metrics != null ? metrics.getAverageDbQueryTime() : 0;
            long jmsSent = metrics != null ? metrics.getJmsMessagesSent() : 0;
            long jmsProc = metrics != null ? metrics.getJmsMessagesProcessed() : 0;
            double avgJms = metrics != null ? metrics.getAverageJmsProcessingTime() : 0;
            long jndiCount = metrics != null ? metrics.getJndiLookups() : 0;
            double avgJndi = metrics != null ? metrics.getAverageJndiTime() : 0;

            long cacheHits = cache != null ? cache.getHits() : 0;
            long cacheMisses = cache != null ? cache.getMisses() : 0;
            double cacheRate = cache != null ? cache.getCacheHitRate() * 100 : 100.0;
        %>

        <div class="metrics-grid">
            <div class="glass-card metric-card">
                <span class="metric-label">HTTP Web Traffic</span>
                <span class="metric-value"><%= reqCount %> reqs</span>
                <span class="metric-sub">Avg latency: <%= String.format("%.2f", avgResp) %> ms</span>
            </div>

            <div class="glass-card metric-card">
                <span class="metric-label">Database Operations</span>
                <span class="metric-value"><%= dbCount %> queries</span>
                <span class="metric-sub">Avg latency: <%= String.format("%.2f", avgDb) %> ms</span>
            </div>

            <div class="glass-card metric-card">
                <span class="metric-label">JMS Asynchronous Queue</span>
                <span class="metric-value"><%= jmsSent %> sent / <%= jmsProc %> proc</span>
                <span class="metric-sub">Avg delivery delay: <%= String.format("%.2f", avgJms) %> ms</span>
            </div>

            <div class="glass-card metric-card">
                <span class="metric-label">JNDI Directory Lookups</span>
                <span class="metric-value"><%= jndiCount %> lookups</span>
                <span class="metric-sub">Avg lookup overhead: <%= String.format("%.2f", avgJndi) %> ms</span>
            </div>
        </div>

        <div class="dashboard-row">

            <div class="glass-card">
                <div class="card-title">
                    <span>Sub-second Inventory Cache (Singleton)</span>
                    <span style="color: #56d364; font-size: 0.9rem;"><%= String.format("%.1f", cacheRate) %>% Hit Rate</span>
                </div>
                <div style="display: flex; flex-direction: column; gap: 10px; margin-top: 10px;">
                    <p>Total cache reads: <strong><%= cacheHits + cacheMisses %></strong></p>
                    <p>Cache Hits: <strong style="color: #56d364;"><%= cacheHits %></strong> (sub-millisecond memory fetches)</p>
                    <p>Cache Misses: <strong style="color: #ff7b72;"><%= cacheMisses %></strong> (database query fallbacks)</p>

                    <div class="progress-bar-container">
                        <div class="progress-bar" style="width: <%= cacheRate %>%;"></div>
                    </div>
                </div>
            </div>

            <div class="glass-card">
                <div class="card-title">
                    <span>Admin Stateful operations context</span>
                    <span style="font-size: 0.8rem; color: #8b949e;">Cleared on Logout</span>
                </div>
                <div class="list-container">
                    <%
                        if (sessionActions != null && !sessionActions.isEmpty()) {
                            for (int i = sessionActions.size() - 1; i >= 0; i--) {
                    %>
                                <div class="log-item" style="border-left: 3px solid #bc8cff;">
                                    <div class="log-header">
                                        <span class="log-type" style="color: #d3b6ff;">Session Action</span>
                                        <span>Time-sync active</span>
                                    </div>
                                    <div class="log-msg"><%= sessionActions.get(i) %></div>
                                </div>
                    <%
                            }
                        } else {
                    %>
                            <p style="color: #8b949e; text-align: center; padding-top: 30px;">No session actions registered.</p>
                    <% } %>
                </div>
            </div>
        </div>

        <div class="glass-card" style="margin-bottom: 30px;">
            <div class="card-title">
                <span>Asynchronous Persistent Audit logs (JMS Pub/Sub Topic)</span>
                <span style="font-size: 0.8rem; color: #8b949e;">Saved to MySQL by AuditMDB</span>
            </div>
            <div class="list-container" style="max-height: 400px;">
                <%
                    if (dbLogs != null && !dbLogs.isEmpty()) {
                        for (AuditLog log : dbLogs) {
                %>
                            <div class="log-item" style="border-left: 3px solid #58a6ff;">
                                <div class="log-header">
                                    <span class="log-type"><%= log.getEventType() %></span>
                                    <span><%= log.getTimestamp().toString() %></span>
                                </div>
                                <div class="log-msg"><%= log.getMessage() %></div>
                            </div>
                <%
                        }
                    } else {
                %>
                        <p style="color: #8b949e; text-align: center; padding-top: 50px;">No persistent audit logs found.</p>
                <% } %>
            </div>
        </div>

    </div>

</body>
</html>
