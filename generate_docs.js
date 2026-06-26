const fs = require('fs');
const path = require('path');
const axios = require('axios');
const { Document, Packer, Paragraph, TextRun, ImageRun, Table, TableRow, TableCell, AlignmentType, HeadingLevel, WidthType, BorderStyle } = require('docx');

const ARTIFACTS_DIR = 'C:\\Users\\Kylie\\.gemini\\antigravity-ide\\brain\\3ba54f1c-eaad-45a7-97db-29c2446856f0';

// Diagrams definition in Mermaid syntax
const DIAGRAMS = {
    testing_diagram: `graph TD
    A[Testing Strategy] --> B[Unit Testing]
    A --> C[Integration Testing]
    A --> D[Load & Performance Testing]
    B --> B1[EJB Business Logic validation]
    B --> B2[JUnit 5 & Mockito subclass mockmaker]
    C --> C1[H2 In-Memory DB & JPA RESOURCE_LOCAL boundaries]
    C --> C2[Asynchronous MDB & JMS Queue/Topic delivery]
    D --> D1[10,000+ Requests Simulation via JMeter]
    D --> D2[Actual CSV logs & JMX test plan validation]`,

    seq_diagram: `sequenceDiagram
    autonumber
    actor Admin as Administrator
    participant Web as OrderServlet (WAR)
    participant EJB as OrderServiceBean (Stateless)
    participant Cache as InventoryCacheBean (Singleton)
    participant DB as MySQL Database
    participant Queue as JMS OrderQueue
    participant MDB as OrderNotificationMDB (MDB)
    participant Async as NotificationService (Async)

    Admin->>Web: Submits Checkout Request
    Web->>EJB: placeOrder(customer, product, qty)
    Note over EJB: Starts Container-Managed Transaction
    EJB->>Cache: getStock(productId)
    Cache-->>EJB: Returns stock from memory
    alt Stock is sufficient
        EJB->>Cache: decrementStock(productId, qty)
        EJB->>DB: Persist Order & OrderItems
        EJB->>Queue: Sends MapMessage (JMS Point-to-Point)
        EJB->>Async: sendAsyncNotification() (non-blocking)
        Note over EJB: Commits Transaction
        EJB-->>Web: Order successful (Render page)
        par JMS Delivery
            Queue->>MDB: Delivers Message (Post-Commit)
            MDB->>MDB: processNotification()
        and Asynchronous Execution
            Async->>Async: Sleep 1.5s & send email
        end
    else Stock insufficient
        EJB-->>Web: Throws InsufficientStockException
        Note over EJB: Rolls back Transaction
        Web-->>Admin: Render error banner
    end`,

    locking_diagram: `sequenceDiagram
    autonumber
    participant DB as MySQL Database
    participant TxA as OrderServiceBean (Tx A)
    participant TxB as OrderServiceBean (Tx B)

    Note over DB: Product 1 (Laptop): Stock = 1, Version = 5
    TxA->>DB: Read Product 1 (Stock=1, Version=5)
    TxB->>DB: Read Product 1 (Stock=1, Version=5)
    
    Note over TxA: Verify Stock (1 >= 1) -> Decrement stock to 0
    TxA->>DB: UPDATE products SET quantity=0, version=6 WHERE id=1 AND version=5
    Note over DB: Version matches! Update succeeds (1 row modified)
    DB-->>TxA: Success (1 row updated)
    Note over TxA: Commit Transaction A

    Note over TxB: Verify Stock (1 >= 1) -> Decrement stock to 0
    TxB->>DB: UPDATE products SET quantity=0, version=6 WHERE id=1 AND version=5
    Note over DB: Version mismatch! (Current DB Version is 6)
    DB-->>TxB: Update fails (0 rows updated)
    Note over TxB: Throws OptimisticLockException
    Note over TxB: Rollback Transaction B`,

    cluster_diagram: `graph LR
    User[Clients] --> LB[Load Balancer]
    LB --> Node1[Payara Node 1]
    LB --> Node2[Payara Node 2]
    Node1 --> Hazelcast[(Hazelcast Clustered Cache)]
    Node2 --> Hazelcast
    Node1 --> DBPool[(MySQL Primary DB)]
    Node2 --> DBPool`,

    er_diagram: `erDiagram
    PRODUCTS {
        long id PK
        string name
        string description
        double price
        int quantity
        boolean deleted
        int version
    }
    ORDERS {
        long id PK
        string customer_name
        datetime order_date
        double total_amount
        string status
    }
    ORDER_ITEMS {
        long id PK
        long order_id FK
        long product_id FK
        int quantity
        double price
    }
    AUDIT_LOGS {
        long id PK
        string action
        string details
        datetime timestamp
    }
    ORDERS ||--o{ ORDER_ITEMS : contains
    PRODUCTS ||--o{ ORDER_ITEMS : referenced_in`,

    usecase_diagram: `graph TD
    subgraph TechMart System
        UC1(Browse Products)
        UC2(Manage Inventory)
        UC3(Place Order / Checkout)
        UC4(View Performance Metrics)
        UC5(View Audit Logs)
        UC6(Receive Asynchronous Notifications)
    end
    
    Customer(Customer) --> UC1
    Customer --> UC3
    
    Admin(Administrator) --> UC2
    Admin --> UC4
    Admin --> UC5
    
    System(EJB Container / JMS) --> UC6
    UC3 --> UC6`
};

async function downloadDiagrams() {
    console.log('Downloading diagrams from mermaid.ink...');
    for (const [name, code] of Object.entries(DIAGRAMS)) {
        const filePath = path.join(ARTIFACTS_DIR, `${name}.png`);
        if (fs.existsSync(filePath)) {
            console.log(`Diagram ${name} already exists. Skipping download.`);
            continue;
        }
        
        try {
            // base64 encode the diagram code directly
            const base64 = Buffer.from(code).toString('base64');
            const url = `https://mermaid.ink/img/${base64}`;
            
            console.log(`Fetching ${name}...`);
            const response = await axios({
                method: 'get',
                url: url,
                responseType: 'stream',
                timeout: 10000
            });
            
            const writer = fs.createWriteStream(filePath);
            response.data.pipe(writer);
            await new Promise((resolve, reject) => {
                writer.on('finish', resolve);
                writer.on('error', reject);
            });
            console.log(`Saved ${name}.png`);
        } catch (error) {
            console.error(`Failed to download diagram ${name}:`, error.message);
        }
    }
}

// Helper to format text with bold and inline code styling
function parseFormattedText(text) {
    const runs = [];
    let i = 0;
    while (i < text.length) {
        if (text.startsWith('**', i)) {
            const next = text.indexOf('**', i + 2);
            if (next !== -1) {
                runs.push(new TextRun({
                    text: text.substring(i + 2, next),
                    bold: true,
                    font: 'Calibri',
                    size: 22
                }));
                i = next + 2;
                continue;
            }
        }
        if (text.startsWith('`', i)) {
            const next = text.indexOf('`', i + 1);
            if (next !== -1) {
                runs.push(new TextRun({
                    text: text.substring(i + 1, next),
                    font: 'Consolas',
                    size: 20,
                    color: 'A52A2A' // Brownish red for inline code
                }));
                i = next + 1;
                continue;
            }
        }
        
        // Find next boundary
        let nextMarker = text.length;
        const nextBold = text.indexOf('**', i);
        const nextCode = text.indexOf('`', i);
        if (nextBold !== -1 && nextBold < nextMarker) nextMarker = nextBold;
        if (nextCode !== -1 && nextCode < nextMarker) nextMarker = nextCode;
        
        runs.push(new TextRun({
            text: text.substring(i, nextMarker),
            font: 'Calibri',
            size: 22
        }));
        i = nextMarker;
    }
    return runs;
}

function parseMarkdownToDocxElements(filePath, docType) {
    const content = fs.readFileSync(filePath, 'utf8');
    const lines = content.split('\n');
    const elements = [];
    
    let inTable = false;
    let tableHeaders = [];
    let tableRows = [];
    let inCodeBlock = false;
    let codeBlockText = '';
    let inList = false;
    let inMermaid = false;
    let mermaidName = '';
    
    for (let i = 0; i < lines.length; i++) {
        let line = lines[i].trim();
        
        // Handle code blocks (including Mermaid)
        if (line.startsWith('```')) {
            if (inCodeBlock) {
                inCodeBlock = false;
                if (inMermaid) {
                    inMermaid = false;
                    // Insert the compiled image
                    const imgPath = path.join(ARTIFACTS_DIR, `${mermaidName}.png`);
                    if (fs.existsSync(imgPath)) {
                        elements.push(new Paragraph({
                            alignment: AlignmentType.CENTER,
                            children: [
                                new ImageRun({
                                    data: fs.readFileSync(imgPath),
                                    transformation: {
                                        width: 450,
                                        height: 280
                                    }
                                })
                            ],
                            spacing: { before: 240, after: 240 }
                        }));
                    }
                } else {
                    // Normal code block
                    elements.push(new Paragraph({
                        children: [
                            new TextRun({
                                text: codeBlockText.trim(),
                                font: 'Consolas',
                                size: 18
                            })
                        ],
                        spacing: { before: 120, after: 120 },
                        indent: { left: 720 },
                        shading: { fill: 'F4F4F4' }
                    }));
                }
                codeBlockText = '';
            } else {
                inCodeBlock = true;
                const lang = line.substring(3).trim();
                if (lang === 'mermaid') {
                    inMermaid = true;
                    // Determine which diagram it is based on the context
                    if (filePath.includes('Technical_Implementation_Documentation')) {
                        if (codeBlockText === '' && elements.length < 50) {
                            mermaidName = 'seq_diagram';
                        } else if (elements.length > 150) {
                            mermaidName = 'locking_diagram';
                        } else {
                            mermaidName = 'cluster_diagram';
                        }
                    } else {
                        mermaidName = 'testing_diagram';
                    }
                    // Reset code block content
                    codeBlockText = '';
                }
            }
            continue;
        }
        
        if (inCodeBlock) {
            if (inMermaid) {
                // Ignore code in mermaid block as we replace it with image
            } else {
                codeBlockText += lines[i] + '\n';
            }
            continue;
        }

        // Handle custom image inclusions for diagrams/screenshots
        if (line.startsWith('![') || line.includes('.png')) {
            // Find diagram injection points or screenshots
            let imgName = '';
            let width = 450;
            let height = 280;
            if (line.includes('login_page')) {
                imgName = 'login_page.png';
            } else if (line.includes('products_page')) {
                imgName = 'products_page.png';
            } else if (line.includes('dashboard_page')) {
                imgName = 'dashboard_page.png';
            }
            
            if (imgName) {
                const imgPath = path.join(ARTIFACTS_DIR, imgName);
                if (fs.existsSync(imgPath)) {
                    elements.push(new Paragraph({
                        alignment: AlignmentType.CENTER,
                        children: [
                            new ImageRun({
                                data: fs.readFileSync(imgPath),
                                transformation: {
                                    width: width,
                                    height: height
                                }
                            })
                        ],
                        spacing: { before: 240, after: 240 }
                    }));
                }
            }
            continue;
        }
        
        // Handle Headings
        if (line.startsWith('# ')) {
            // Main title - ignored since we use custom cover page
            continue;
        }
        if (line.startsWith('## ')) {
            const title = line.substring(3).trim();
            elements.push(new Paragraph({
                text: title,
                heading: HeadingLevel.HEADING_2,
                spacing: { before: 240, after: 120 },
                keepWithNext: true,
                children: [
                    new TextRun({
                        text: title,
                        bold: true,
                        font: 'Calibri',
                        size: 28 // 14pt
                    })
                ]
            }));
            // Special injections for diagrams that were added as sections
            if (docType === 'tech' && title.includes('System Architecture and Component Relationships')) {
                // Insert ER Diagram and Use Case Diagram here
                const erPath = path.join(ARTIFACTS_DIR, 'er_diagram.png');
                const ucPath = path.join(ARTIFACTS_DIR, 'usecase_diagram.png');
                
                elements.push(new Paragraph({
                    text: 'Database ER Diagram',
                    heading: HeadingLevel.HEADING_3,
                    spacing: { before: 180, after: 90 },
                    children: [
                        new TextRun({
                            text: 'Database ER Diagram',
                            bold: true,
                            font: 'Calibri',
                            size: 24 // 12pt
                        })
                    ]
                }));
                if (fs.existsSync(erPath)) {
                    elements.push(new Paragraph({
                        alignment: AlignmentType.CENTER,
                        children: [
                            new ImageRun({
                                data: fs.readFileSync(erPath),
                                transformation: { width: 450, height: 280 }
                            })
                        ],
                        spacing: { before: 120, after: 120 }
                    }));
                }
                
                elements.push(new Paragraph({
                    text: 'Use Case Diagram',
                    heading: HeadingLevel.HEADING_3,
                    spacing: { before: 180, after: 90 },
                    children: [
                        new TextRun({
                            text: 'Use Case Diagram',
                            bold: true,
                            font: 'Calibri',
                            size: 24 // 12pt
                        })
                    ]
                }));
                if (fs.existsSync(ucPath)) {
                    elements.push(new Paragraph({
                        alignment: AlignmentType.CENTER,
                        children: [
                            new ImageRun({
                                data: fs.readFileSync(ucPath),
                                transformation: { width: 450, height: 280 }
                            })
                        ],
                        spacing: { before: 120, after: 120 }
                    }));
                }
            }
            continue;
        }
        if (line.startsWith('### ')) {
            const title = line.substring(4).trim();
            elements.push(new Paragraph({
                text: title,
                heading: HeadingLevel.HEADING_3,
                spacing: { before: 180, after: 90 },
                keepWithNext: true,
                children: [
                    new TextRun({
                        text: title,
                        bold: true,
                        font: 'Calibri',
                        size: 24 // 12pt
                    })
                ]
            }));
            continue;
        }
        if (line.startsWith('#### ')) {
            const title = line.substring(5).trim();
            elements.push(new Paragraph({
                text: title,
                heading: HeadingLevel.HEADING_4,
                spacing: { before: 120, after: 60 },
                keepWithNext: true,
                children: [
                    new TextRun({
                        text: title,
                        bold: true,
                        font: 'Calibri',
                        size: 22 // 11pt bold
                    })
                ]
            }));
            continue;
        }
        
        // Handle Tables
        if (line.startsWith('|')) {
            inTable = true;
            const cells = line.split('|').map(c => c.trim()).filter((c, idx, arr) => idx > 0 && idx < arr.length - 1);
            if (line.includes('---')) {
                // Table separator, skip
                continue;
            }
            if (tableHeaders.length === 0) {
                tableHeaders = cells;
            } else {
                tableRows.push(cells);
            }
            continue;
        } else if (inTable) {
            inTable = false;
            // Build the table element
            const docxRows = [];
            
            // Header Row
            docxRows.push(new TableRow({
                children: tableHeaders.map(cellText => new TableCell({
                    children: [new Paragraph({
                        children: [new TextRun({ text: cellText, bold: true, font: 'Calibri', size: 20 })],
                        alignment: AlignmentType.LEFT
                    })],
                    shading: { fill: 'EAEAEA' }
                }))
            }));
            
            // Data Rows
            tableRows.forEach(row => {
                docxRows.push(new TableRow({
                    children: row.map(cellText => new TableCell({
                        children: [new Paragraph({
                            children: parseFormattedText(cellText),
                            alignment: AlignmentType.LEFT
                        })]
                    }))
                }));
            });
            
            elements.push(new Table({
                rows: docxRows,
                width: { size: 100, type: WidthType.PERCENTAGE },
                spacing: { before: 240, after: 240 },
                borders: {
                    top: { style: BorderStyle.SINGLE, size: 4, color: 'D0D0D0' },
                    bottom: { style: BorderStyle.SINGLE, size: 4, color: 'D0D0D0' },
                    left: { style: BorderStyle.SINGLE, size: 4, color: 'D0D0D0' },
                    right: { style: BorderStyle.SINGLE, size: 4, color: 'D0D0D0' },
                    insideHorizontal: { style: BorderStyle.SINGLE, size: 4, color: 'E8E8E8' },
                    insideVertical: { style: BorderStyle.SINGLE, size: 4, color: 'E8E8E8' }
                }
            }));
            
            tableHeaders = [];
            tableRows = [];
            continue;
        }
        
        // Handle Bullet Lists
        if (line.startsWith('* ') || line.startsWith('- ')) {
            inList = true;
            const text = line.substring(2).trim();
            elements.push(new Paragraph({
                children: parseFormattedText(text),
                bullet: { level: 0 },
                spacing: { before: 60, after: 60 },
                alignment: AlignmentType.JUSTIFIED
            }));
            continue;
        }
        
        // Handle Blockquotes (Alerts)
        if (line.startsWith('>')) {
            let text = line.substring(1).trim();
            if (text.startsWith('[!')) {
                // Alert header, read next line
                continue;
            }
            elements.push(new Paragraph({
                children: [
                    new TextRun({
                        text: text,
                        font: 'Calibri',
                        size: 22,
                        italics: true
                    })
                ],
                spacing: { before: 120, after: 120 },
                indent: { left: 720 },
                shading: { fill: 'F9F9F9' }
            }));
            continue;
        }
        
        // Empty lines or dividers
        if (line === '' || line === '---') {
            continue;
        }
        
        // Regular Paragraph
        elements.push(new Paragraph({
            children: parseFormattedText(line),
            spacing: { before: 120, after: 120, line: 360 }, // 1.5 line spacing (360 dxa)
            alignment: AlignmentType.JUSTIFIED
        }));
    }
    
    return elements;
}

// Function to generate the cover page
function createCoverPage(titleText) {
    return [
        new Paragraph({
            alignment: AlignmentType.CENTER,
            children: [
                new TextRun({
                    text: 'JAVA INSTITUTE FOR ADVANCED TECHNOLOGY',
                    bold: true,
                    font: 'Calibri',
                    size: 32 // 16pt
                })
            ],
            spacing: { before: 480, after: 120 }
        }),
        new Paragraph({
            alignment: AlignmentType.CENTER,
            children: [
                new TextRun({
                    text: 'Department of Examinations',
                    font: 'Calibri',
                    size: 24, // 12pt
                    italics: true
                })
            ],
            spacing: { after: 1440 } // Large gap
        }),
        new Paragraph({
            alignment: AlignmentType.CENTER,
            children: [
                new TextRun({
                    text: titleText.toUpperCase(),
                    bold: true,
                    font: 'Calibri',
                    size: 36 // 18pt
                })
            ],
            spacing: { before: 720, after: 2160 } // Huge gap
        }),
        new Paragraph({
            alignment: AlignmentType.LEFT,
            children: [
                new TextRun({ text: 'Student Name: ', bold: true, font: 'Calibri', size: 22 }),
                new TextRun({ text: 'Kylie', font: 'Calibri', size: 22 })
            ],
            spacing: { before: 120, after: 120 }
        }),
        new Paragraph({
            alignment: AlignmentType.LEFT,
            children: [
                new TextRun({ text: 'NIC No: ', bold: true, font: 'Calibri', size: 22 }),
                new TextRun({ text: '992345678V', font: 'Calibri', size: 22 })
            ],
            spacing: { before: 120, after: 120 }
        }),
        new Paragraph({
            alignment: AlignmentType.LEFT,
            children: [
                new TextRun({ text: 'Subject Name: ', bold: true, font: 'Calibri', size: 22 }),
                new TextRun({ text: 'Business Component Development I', font: 'Calibri', size: 22 })
            ],
            spacing: { before: 120, after: 120 }
        }),
        new Paragraph({
            alignment: AlignmentType.LEFT,
            children: [
                new TextRun({ text: 'Subject Code: ', bold: true, font: 'Calibri', size: 22 }),
                new TextRun({ text: 'JIAT/BCD I/EX/01', font: 'Calibri', size: 22 })
            ],
            spacing: { before: 120, after: 120 }
        }),
        new Paragraph({
            alignment: AlignmentType.LEFT,
            children: [
                new TextRun({ text: 'Branch: ', bold: true, font: 'Calibri', size: 22 }),
                new TextRun({ text: 'Colombo Campus', font: 'Calibri', size: 22 })
            ],
            spacing: { before: 120, after: 2160 } // Large gap before page break
        }),
        new Paragraph({
            alignment: AlignmentType.CENTER,
            children: [
                new TextRun({
                    text: 'Date: June 2026',
                    font: 'Calibri',
                    size: 22
                })
            ]
        })
    ];
}

async function main() {
    // 1. Download all required diagrams
    await downloadDiagrams();
    
    // 2. Generate Technical Implementation Documentation DOCX
    console.log('Generating Technical_Implementation_Documentation.docx...');
    const techDoc = new Document({
        sections: [{
            properties: {
                page: {
                    margin: {
                        top: 1440, // 1 inch
                        bottom: 1440,
                        left: 1440,
                        right: 1440
                    }
                }
            },
            children: [
                ...createCoverPage('Technical Implementation Documentation'),
                new Paragraph({ text: '', pageBreakBefore: true }), // Page break after cover page
                ...parseMarkdownToDocxElements('Technical_Implementation_Documentation.md', 'tech')
            ]
        }]
    });
    
    Packer.toBuffer(techDoc).then((buffer) => {
        fs.writeFileSync('Technical_Implementation_Documentation.docx', buffer);
        console.log('Saved Technical_Implementation_Documentation.docx');
    });

    // 3. Generate Critical Analysis and Test Report DOCX
    console.log('Generating Critical_Analysis_Test_Report.docx...');
    
    // Inject local screenshots into Critical Analysis report
    // We will append screenshots in Section 1.3 or Section 2
    const testDoc = new Document({
        sections: [{
            properties: {
                page: {
                    margin: {
                        top: 1440, // 1 inch
                        bottom: 1440,
                        left: 1440,
                        right: 1440
                    }
                }
            },
            children: [
                ...createCoverPage('Critical Analysis & Test Report'),
                new Paragraph({ text: '', pageBreakBefore: true }), // Page break after cover page
                ...parseMarkdownToDocxElements('Critical_Analysis_Test_Report.md', 'test')
            ]
        }]
    });
    
    Packer.toBuffer(testDoc).then((buffer) => {
        fs.writeFileSync('Critical_Analysis_Test_Report.docx', buffer);
        console.log('Saved Critical_Analysis_Test_Report.docx');
    });
}

main().catch(err => {
    console.error('Error during execution:', err);
});
