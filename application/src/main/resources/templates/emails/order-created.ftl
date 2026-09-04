<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Nouvelle commande reçue</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=DM+Sans:ital,wght@0,500;0,600;0,700;1,400&family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <style>
        /* Palette issue de votre theme CSS */
        body {
            margin: 0;
            padding: 0;
            background-color: #fdf8f8; /* hsl(355 30% 97%) - --background */
            font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
            -webkit-font-smoothing: antialiased;
            color: #2b1216; /* hsl(350 40% 12%) - --foreground */
        }
        h1, h2, h3, .heading-font {
            font-family: 'DM Sans', -apple-system, BlinkMacSystemFont, sans-serif;
        }
        .wrapper {
            width: 100%;
            table-layout: fixed;
            background-color: #fdf8f8;
            padding: 40px 0;
        }
        .main-table {
            background-color: #fffefe; /* hsl(355 25% 99%) - --card */
            margin: 0 auto;
            width: 100%;
            max-width: 640px;
            border-spacing: 0;
            border: 1px solid #ebdbe0; /* hsl(350 18% 89%) - --border */
            border-radius: 16px; /* 1rem - --radius */
            overflow: hidden;
            box-shadow: 0 2px 20px -4px rgba(203, 24, 74, 0.08); /* --shadow-card */
        }
        .header {
            /* --gradient-hero */
            background: linear-gradient(135deg, #cb184a, #d14686, #e24e70);
            color: #ffffff;
            padding: 28px 32px;
        }
        .badge-success {
            display: inline-block;
            background-color: #339970; /* hsl(160 50% 40%) - --success */
            color: #ffffff;
            font-size: 11px;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            padding: 4px 12px;
            border-radius: 9999px;
            margin-bottom: 12px;
        }
        .header h1 {
            margin: 0;
            font-size: 22px;
            font-weight: 700;
            color: #ffffff;
            letter-spacing: -0.02em;
        }
        .content {
            padding: 32px;
        }
        .section-title {
            font-size: 12px;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            color: #83676e; /* hsl(345 12% 46%) - --muted-foreground */
            margin: 0 0 12px 0;
        }
        .grid-info {
            width: 100%;
            border-collapse: separate;
            border-spacing: 0;
            margin-bottom: 24px;
            background-color: #f9edee; /* hsl(350 35% 93%) - --secondary */
            border: 1px solid #ebdbe0; /* hsl(350 18% 89%) - --border */
            border-radius: 12px;
        }
        .grid-info td {
            padding: 12px 16px;
            font-size: 14px;
            border-bottom: 1px solid #ebdbe0;
        }
        .grid-info tr:last-child td {
            border-bottom: none;
        }
        .label {
            color: #83676e; /* --muted-foreground */
            font-weight: 500;
            width: 35%;
        }
        .value {
            color: #2b1216; /* --foreground */
            font-weight: 600;
        }

        /* Nouveau style pour le message du client */
        .message-box {
            background-color: #f9edee; /* --secondary */
            border: 1px solid #ebdbe0;
            border-left: 4px solid #cb184a; /* accentuation avec --primary */
            border-radius: 8px;
            padding: 14px 16px;
            margin-bottom: 28px;
        }
        .message-text {
            margin: 0;
            font-size: 13px;
            color: #2b1216;
            line-height: 1.5;
            font-style: italic;
            white-space: pre-wrap; /* Conserve les sauts de ligne rédigés par le client */
        }

        .items-table {
            width: 100%;
            border-collapse: collapse;
            margin-bottom: 24px;
        }
        .items-table th {
            background-color: #f4ebec; /* hsl(350 18% 92%) - --muted */
            color: #83676e; /* --muted-foreground */
            font-size: 11px;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            padding: 10px 12px;
            text-align: left;
            border-top: 1px solid #ebdbe0;
            border-bottom: 1px solid #ebdbe0;
        }
        .items-table td {
            padding: 14px 12px;
            border-bottom: 1px solid #ebdbe0;
            font-size: 13px;
            color: #2b1216;
            vertical-align: top;
        }
        .text-right {
            text-align: right;
        }
        .meta-tag {
            display: inline-block;
            font-size: 11px;
            color: #3f1a23; /* hsl(345 50% 25%) - --accent-foreground */
            background-color: #f2d8dc; /* hsl(350 50% 88%) - --accent */
            padding: 2px 8px;
            border-radius: 6px;
            margin-top: 4px;
            font-weight: 500;
        }
        .discount-tag {
            display: inline-block;
            font-size: 11px;
            color: #dc2828; /* hsl(0 72% 51%) - --destructive */
            background-color: #fde8e8;
            padding: 2px 8px;
            border-radius: 6px;
            margin-top: 4px;
            font-weight: 600;
        }
        .total-box {
            background-color: #f9edee; /* --secondary */
            border: 1px solid #ebdbe0;
            border-radius: 12px;
            padding: 16px 20px;
            text-align: right;
        }
        .total-label {
            font-size: 14px;
            font-weight: 600;
            color: #83676e;
        }
        .total-amount {
            font-size: 22px;
            font-weight: 700;
            color: #cb184a; /* hsl(345 80% 46%) - --primary */
            margin-left: 12px;
        }
        .footer {
            background-color: #f9edee;
            padding: 18px 32px;
            text-align: center;
            font-size: 12px;
            color: #83676e;
            border-top: 1px solid #ebdbe0;
        }
        .code-block {
            font-family: ui-monospace, SFMono-Regular, Consolas, monospace;
            background-color: #f4ebec;
            color: #cb184a;
            padding: 2px 6px;
            border-radius: 4px;
            font-size: 13px;
            font-weight: 600;
        }
    </style>
</head>
<body>

<div class="wrapper">
    <table class="main-table">
        <!-- Header Hero -->
        <tr>
            <td class="header">
                <span class="badge-success">Alerte Système</span>
                <h1 class="heading-font">Nouvelle commande reçue</h1>
            </td>
        </tr>

        <!-- Content -->
        <tr>
            <td class="content">

                <!-- Client & Order Info -->
                <p class="section-title heading-font">Informations Générales</p>
                <table class="grid-info">
                    <tr>
                        <td class="label">N° Commande</td>
                        <td class="value"><span class="code-block">${orderId}</span></td>
                    </tr>
                    <tr>
                        <td class="label">Date & Heure</td>
                        <td class="value">${createdAt} UTC</td>
                    </tr>
                    <tr>
                        <td class="label">Nom du Client</td>
                        <td class="value">${customerName}</td>
                    </tr>
                    <tr>
                        <td class="label">Email du Client</td>
                        <td class="value"><a href="mailto:${customerEmail}" style="color: #cb184a; text-decoration: underline;">${customerEmail}</a></td>
                    </tr>
                </table>

                <!-- Message du client (Affiché uniquement s'il existe et n'est pas vide) -->
                <#if customerMessage?? && customerMessage?has_content>
                    <div class="message-box">
                        <p class="section-title heading-font" style="margin-bottom: 6px; color: #cb184a;">Message du client :</p>
                        <p class="message-text">"${customerMessage}"</p>
                    </div>
                </#if>

                <!-- Items Details -->
                <p class="section-title heading-font">Articles Commandés (${items?size})</p>
                <table class="items-table">
                    <thead>
                        <tr>
                            <th class="heading-font">Produit</th>
                            <th class="text-right heading-font">Qté</th>
                            <th class="text-right heading-font">P.U.</th>
                            <th class="text-right heading-font">Total</th>
                        </tr>
                    </thead>
                    <tbody>
                        <#list items as item>
                            <tr>
                                <td>
                                    <strong class="heading-font">${item.productName()}</strong>
                                    <#if item.chosenColor()??>
                                        <br><span class="meta-tag">Couleur : ${item.chosenColor()}</span>
                                    </#if>
                                    <#if item.discountRate()?? && item.discountRate() gt 0>
                                        <br><span class="discount-tag">Remise : -${(item.discountRate())?string["0.##"]}%</span>
                                    </#if>
                                </td>
                                <td class="text-right">${item.quantity()}</td>
                                <td class="text-right">${item.unitPrice()?string["0.00"]} €</td>
                                <td class="text-right"><strong class="heading-font">${item.totalAmount()?string["0.00"]} €</strong></td>
                            </tr>
                        </#list>
                    </tbody>
                </table>

                <!-- Total -->
                <div class="total-box">
                    <span class="total-label heading-font">Montant Total :</span>
                    <span class="total-amount heading-font">${total?string["0.00"]} €</span>
                </div>

            </td>
        </tr>

        <!-- Footer -->
        <tr>
            <td class="footer">
                Notification automatique envoyée par l'administration du site.
            </td>
        </tr>
    </table>
</div>

</body>
</html>