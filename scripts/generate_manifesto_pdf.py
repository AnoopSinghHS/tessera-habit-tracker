import os
import sys
from reportlab.lib.pagesizes import letter
from reportlab.lib import colors
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, HRFlowable
)
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.pdfgen import canvas

class NumberedCanvas(canvas.Canvas):
    def __init__(self, *args, **kwargs):
        super(NumberedCanvas, self).__init__(*args, **kwargs)
        self._saved_page_states = []

    def showPage(self):
        self._saved_page_states.append(dict(self.__dict__))
        self._startPage()

    def save(self):
        num_pages = len(self._saved_page_states)
        for state in self._saved_page_states:
            self.__dict__.update(state)
            self.draw_page_decorations(num_pages)
            super(NumberedCanvas, self).showPage()
        super(NumberedCanvas, self).save()

    def draw_page_decorations(self, page_count):
        self.saveState()
        self.setFont("Helvetica", 8)
        self.setFillColor(colors.HexColor("#78746D"))

        # Running header (pages 2+)
        if self._pageNumber > 1:
            self.drawString(48, 750, "TESSERA MANIFESTO — THE TRIAD OF COHESIVE ACTION")
            self.setStrokeColor(colors.HexColor("#E8E4DC"))
            self.setLineWidth(0.5)
            self.line(48, 742, 564, 742)

        # Running footer
        page_str = f"Page {self._pageNumber} of {page_count}"
        self.drawRightString(564, 32, page_str)
        self.drawString(48, 32, "Confidential • Architectural Whitepaper • v1.0.0")
        self.restoreState()

def build_pdf(filename="dist/The_Tessera_Manifesto.pdf"):
    os.makedirs(os.path.dirname(filename), exist_ok=True)
    doc = SimpleDocTemplate(
        filename,
        pagesize=letter,
        leftMargin=48,
        rightMargin=48,
        topMargin=46,
        bottomMargin=46
    )

    styles = getSampleStyleSheet()

    # Custom Palette
    COLOR_CARBON = colors.HexColor("#121210")
    COLOR_CHARCOAL = colors.HexColor("#1C1B19")
    COLOR_STONE = colors.HexColor("#78746D")
    COLOR_BORDER = colors.HexColor("#E8E4DC")
    COLOR_BG_LIGHT = colors.HexColor("#F5F3EF")
    COLOR_FOREST = colors.HexColor("#2D5A43")

    style_cover_pre = ParagraphStyle(
        'CoverPre',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=9,
        leading=12,
        textColor=COLOR_FOREST,
        spaceAfter=6,
        textTransform='uppercase'
    )

    style_title = ParagraphStyle(
        'MainTitle',
        parent=styles['Normal'],
        fontName='Times-Bold',
        fontSize=26,
        leading=30,
        textColor=COLOR_CHARCOAL,
        spaceAfter=6
    )

    style_subtitle = ParagraphStyle(
        'MainSubTitle',
        parent=styles['Normal'],
        fontName='Times-Italic',
        fontSize=13,
        leading=17,
        textColor=COLOR_STONE,
        spaceAfter=14
    )

    style_meta = ParagraphStyle(
        'Meta',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=8.5,
        leading=12,
        textColor=COLOR_STONE
    )

    style_h1 = ParagraphStyle(
        'Heading1_Custom',
        parent=styles['Heading1'],
        fontName='Times-Bold',
        fontSize=17,
        leading=21,
        textColor=COLOR_CHARCOAL,
        spaceBefore=14,
        spaceAfter=6,
        keepWithNext=True
    )

    style_h2 = ParagraphStyle(
        'Heading2_Custom',
        parent=styles['Heading2'],
        fontName='Times-Bold',
        fontSize=11.5,
        leading=15,
        textColor=COLOR_FOREST,
        spaceBefore=10,
        spaceAfter=4,
        keepWithNext=True
    )

    style_body = ParagraphStyle(
        'Body_Custom',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=9,
        leading=13.5,
        textColor=COLOR_CHARCOAL,
        spaceAfter=7
    )

    style_body_bold = ParagraphStyle(
        'Body_Bold',
        parent=style_body,
        fontName='Helvetica-Bold'
    )

    style_code = ParagraphStyle(
        'CodeSnippet',
        parent=styles['Normal'],
        fontName='Courier',
        fontSize=8,
        leading=11,
        textColor=COLOR_CHARCOAL
    )

    story = []

    # ================= PAGE 1: COVER & COMPARATIVE SYNTHESIS =================
    story.append(Paragraph("TESSERA ARCHITECTURAL WHITEPAPER", style_cover_pre))
    story.append(Paragraph("The Architecture of Human Ritual", style_title))
    story.append(Paragraph("Synthesizing Atoms, HabitKit, and Fabulous into an Uncluttered Ritual Engine", style_subtitle))
    story.append(HRFlowable(width="100%", thickness=1.5, color=COLOR_FOREST, spaceBefore=0, spaceAfter=10))

    meta_text = "<b>Author:</b> DeepMind Agentic Pair Architecture • <b>Device Focus:</b> Samsung Galaxy S26 Ultra & Web Platform • <b>Version:</b> 1.0.0"
    story.append(Paragraph(meta_text, style_meta))
    story.append(Spacer(1, 10))

    exec_summary = (
        "<b>Executive Synthesis:</b> Most habit trackers suffer from an acute identity crisis. "
        "They either reduce personal growth to a sterile database checklist (lacking behavioral guidance), "
        "or drown the user in cartoonish gamification, intrusive animations, and pseudo-psychological guilt loops. "
        "<br/><br/>"
        "<b>Tessera</b> presents a third way: <b>The Triad of Cohesive Action</b>. By treating <b>James Clear's Atoms</b> as the underlying behavioral mechanics, "
        "<b>Fabulous</b> as the temporal sequence choreography, and <b>HabitKit</b> as the architectural visual proof, Tessera harmonizes these philosophies "
        "without conceptual clash. Zero emojis. Zero neon clutter. 100% offline-first resilience."
    )
    story.append(Paragraph(exec_summary, style_body))
    story.append(Spacer(1, 10))

    # Core Comparison Matrix Table (engineered to fit Page 1 perfectly)
    comparison_data = [
        [
            Paragraph("<b>Dimension</b>", style_body_bold),
            Paragraph("<b>HabitKit</b>", style_body_bold),
            Paragraph("<b>Fabulous</b>", style_body_bold),
            Paragraph("<b>Tessera (Unified)</b>", style_body_bold)
        ],
        [
            Paragraph("<b>Core Philosophy</b>", style_meta),
            Paragraph("Visual data tracking & raw grid utility.", style_meta),
            Paragraph("Behavioral science coaching & story arcs.", style_meta),
            Paragraph("<b>The Triad</b>: Behavioral logic + temporal choreography + architectural proof.", style_meta)
        ],
        [
            Paragraph("<b>Habit Stacking</b>", style_meta),
            Paragraph("None (floating independent items).", style_meta),
            Paragraph("Rigid, forced onboarding sequences.", style_meta),
            Paragraph("<b>Habit Stack Weaver</b>: Guided visual cue, micro-starter, and identity bonds.", style_meta)
        ],
        [
            Paragraph("<b>Visual Tone</b>", style_meta),
            Paragraph("Neon tiles, boxy cards, emojis.", style_meta),
            Paragraph("Heavy story illustrations, audio clutter.", style_meta),
            Paragraph("<b>Dieter Rams / Things 3</b>: Warm Limestone, Matte Carbon, Dune, 40+ Lucide icons.", style_meta)
        ],
        [
            Paragraph("<b>Execution Flow</b>", style_meta),
            Paragraph("Manual checklist toggle.", style_meta),
            Paragraph("Audio coaching guided session.", style_meta),
            Paragraph("<b>The Ritual Chamber</b>: Distraction-free kinetic stepper with 2-min micro-timer.", style_meta)
        ],
        [
            Paragraph("<b>Platform Sync</b>", style_meta),
            Paragraph("Device local / basic iCloud.", style_meta),
            Paragraph("Cloud-only account locking.", style_meta),
            Paragraph("<b>Offline-First LWW</b>: Room DB + Web SQLite + JWT bi-directional sync.", style_meta)
        ]
    ]

    comp_table = Table(comparison_data, colWidths=[95, 125, 125, 171])
    comp_table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), COLOR_BG_LIGHT),
        ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
        ('VALIGN', (0, 0), (-1, -1), 'TOP'),
        ('TEXTCOLOR', (0, 0), (-1, -1), COLOR_CHARCOAL),
        ('GRID', (0, 0), (-1, -1), 0.5, COLOR_BORDER),
        ('TOPPADDING', (0, 0), (-1, -1), 4.5),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 4.5),
        ('LEFTPADDING', (0, 0), (-1, -1), 5),
        ('RIGHTPADDING', (0, 0), (-1, -1), 5),
    ]))
    story.append(comp_table)

    story.append(PageBreak())

    # ================= PAGE 2: THE TRIAD & GUIDED UI =================
    story.append(Paragraph("1. The Triad of Cohesive Action", style_h1))
    story.append(Paragraph(
        "To prevent multiple productivity paradigms from degenerating into a chaotic mess, Tessera assigns each philosophy to a distinct, non-overlapping architectural layer:",
        style_body
    ))

    triad_p1 = (
        "<b>1. The Behavioral Logic (Atoms / James Clear) — The WHY and HOW:</b><br/>"
        "Habits cannot be formed in an environmental vacuum. Atoms provides the behavioral mechanics: "
        "the 4 Laws of Behavior Change. Every habit in Tessera requires an <b>Anchor Cue</b> (<i>'After [event]'</i>), "
        "a <b>2-Minute Micro-Starter</b> to eliminate friction, and an <b>Identity Statement</b> (<i>'I am the type of person who...'</i>). "
        "This gives structural integrity to every ritual."
    )
    story.append(Paragraph(triad_p1, style_body))

    triad_p2 = (
        "<b>2. The Temporal Choreography (Fabulous / Dan Ariely) — The WHEN and FLOW:</b><br/>"
        "Floating checklists cause decision fatigue. Borrowing the core breakthrough of Duke University's Center for Advanced Hindsight in Fabulous, "
        "Tessera organizes habits into <b>Ritual Tracks</b>: The Dawn Sequence (Morning), The Deep Focus Track (Midday), and The Twilight Wind-Down (Evening). "
        "Instead of asking the user to manually check off a disorganized list, the user enters <b>The Ritual Chamber</b>, where habits are executed in a calm, unbroken chain."
    )
    story.append(Paragraph(triad_p2, style_body))

    triad_p3 = (
        "<b>3. The Architectural Proof (HabitKit & Tessera) — WHAT PROGRESS LOOKS LIKE:</b><br/>"
        "Where Fabulous relies on ephemeral story text, Tessera anchors long-term commitment in the permanent, satisfying reality of "
        "the <b>GitHub Contribution Matrix</b>. Every step completed in a ritual illuminates an organic pigment square across the user's "
        "Galaxy S26 Ultra home screen widgets, web dashboard, and insights telemetry."
    )
    story.append(Paragraph(triad_p3, style_body))

    story.append(Spacer(1, 8))
    story.append(Paragraph("2. The Guided UI: The Habit Stack Weaver & Ritual Chamber", style_h1))
    story.append(Paragraph(
        "Rather than forcing the user to read passive text, Tessera translates behavioral psychology into interactive, kinetic UI components:",
        style_body
    ))

    story.append(Paragraph("A. The Habit Stack Weaver", style_h2))
    story.append(Paragraph(
        "The Weaver replaces generic 'New Task' dialogs with an architectural sequence wizard. "
        "As the user types, visual connector links dynamically render the causal chain: "
        "<br/><code>[Anchor Cue: Brewing morning coffee] --> [Micro-Starter: 120s vocal hum] --> [Habit: Vocal Riyaaz] --> [Identity: Disciplined Vocalist]</code>",
        style_body
    ))

    story.append(Paragraph("B. The Ritual Chamber", style_h2))
    story.append(Paragraph(
        "Tapping 'Begin Ritual' transitions the interface into a distraction-free execution chamber. "
        "Key elements include: "
        "<br/>• <b>Active Micro-Timer:</b> A minimal 120-second countdown for the 2-minute micro-starter to ensure the user crosses the friction barrier."
        "<br/>• <b>Identity Affirmation:</b> Rendered in refined serif typography to mentally solidify the habit before physical execution."
        "<br/>• <b>Kinetic Advancement:</b> Tapping completion pulses the contribution matrix and smoothly glides to the next link in the chain.",
        style_body
    ))

    story.append(PageBreak())

    # ================= PAGE 3: TECHNICAL ARCHITECTURE & DEPLOYMENT =================
    story.append(Paragraph("3. Technical Architecture: Mobile, Web & Cloud Sync", style_h1))
    story.append(Paragraph(
        "Tessera is built on an offline-first distributed architecture with guaranteed eventual consistency.",
        style_body
    ))

    tech_data = [
        [Paragraph("<b>Component</b>", style_body_bold), Paragraph("<b>Tech Stack</b>", style_body_bold), Paragraph("<b>Key Responsibilities</b>", style_body_bold)],
        [
            Paragraph("<b>Mobile App</b>", style_meta),
            Paragraph("Native Kotlin, Jetpack Compose, Material 3, Room DB", style_meta),
            Paragraph("Targeted for Samsung Galaxy S26 Ultra (One UI 9). Edge-to-edge status bar padding, 22-week wide widgets, 40+ vector icons.", style_meta)
        ],
        [
            Paragraph("<b>Web App</b>", style_meta),
            Paragraph("Vite, React 18, TypeScript, Custom Swiss CSS", style_meta),
            Paragraph("Full desktop experience mirroring mobile design tokens, SVG GitHub heatmap, Habit Stack Weaver modal, and Ritual Chamber.", style_meta)
        ],
        [
            Paragraph("<b>Sync Backend</b>", style_meta),
            Paragraph("Node.js 22+, Express, better-sqlite3, JWT, Docker", style_meta),
            Paragraph("Atomic bi-directional synchronization, bcrypt password hashing, Last-Write-Wins (LWW) conflict engine, Dockerized.", style_meta)
        ],
        [
            Paragraph("<b>Widgets</b>", style_meta),
            Paragraph("AppWidgetProvider, High-DPI Canvas Bitmap", style_meta),
            Paragraph("Instant 1-tap check-in widgets without app launch. Automatic widget broadcast invalidation upon sync.", style_meta)
        ]
    ]

    tech_table = Table(tech_data, colWidths=[90, 140, 286])
    tech_table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), COLOR_BG_LIGHT),
        ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
        ('VALIGN', (0, 0), (-1, -1), 'TOP'),
        ('GRID', (0, 0), (-1, -1), 0.5, COLOR_BORDER),
        ('TOPPADDING', (0, 0), (-1, -1), 4.5),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 4.5),
        ('LEFTPADDING', (0, 0), (-1, -1), 6),
        ('RIGHTPADDING', (0, 0), (-1, -1), 6),
    ]))
    story.append(tech_table)

    story.append(Spacer(1, 10))
    story.append(Paragraph("A. The Mac Desktop Widget Inquiry", style_h2))
    mac_analysis = (
        "<b>Can Mac Desktop Widgets be implemented?</b><br/>"
        "On macOS Sonoma and Sequoia, desktop widgets are powered exclusively by Apple's native <b>WidgetKit</b>. "
        "A browser-based web application cannot directly register an interactive desktop widget onto the macOS desktop. "
        "However, there are two production pathways to deliver Mac widgets for Tessera:<br/>"
        "1. <b>iOS/iPad Companion on Mac:</b> Because Apple Silicon Macs run iPadOS apps natively, an iPad-compatible build of Tessera with WidgetKit appears directly in the macOS Notification Center and desktop widget gallery.<br/>"
        "2. <b>SwiftUI Menu-Bar Companion:</b> A 5MB lightweight native Swift menu-bar app that connects to the Tessera Sync API (<b>/api/sync</b>) and renders the GitHub heatmap in the macOS status bar."
    )
    story.append(Paragraph(mac_analysis, style_body))

    story.append(Spacer(1, 6))
    story.append(Paragraph("B. Cloud Sync Protocol (Last-Write-Wins)", style_h2))
    sync_code = (
        "POST /api/sync\n"
        "Headers: Authorization: Bearer &lt;JWT&gt;\n"
        "Payload: {\n"
        "  lastSyncTimestamp: 1760600000000,\n"
        "  habits: [ { slug: 'vocal-riyaaz', updatedAt: 1760630000000, ... } ],\n"
        "  logs:   [ { habitSlug: 'vocal-riyaaz', count: 1, dateEpochDay: 20743, ... } ]\n"
        "}\n"
        "Response: 200 OK -> { serverTimestamp, habits: [...newerOnServer], logs: [...] }"
    )
    story.append(Paragraph(f"<pre>{sync_code}</pre>", style_code))

    story.append(Spacer(1, 10))
    story.append(Paragraph("4. Conclusion: A Timeless Daily Ritual", style_h1))
    conclusion_text = (
        "Tessera demonstrates that high behavioral fidelity does not require visual bloat. "
        "By grounding behavioral science (Atoms) into kinetic daily rhythm (Fabulous) and honoring human focus with "
        "quiet, typographic craft (Dieter Rams & HabitKit), Tessera establishes a calm, sustainable habit sanctuary "
        "engineered for decades of daily practice."
    )
    story.append(Paragraph(conclusion_text, style_body))

    doc.build(story, canvasmaker=NumberedCanvas)
    print(f"Manifesto PDF compiled successfully to {filename}")

if __name__ == '__main__':
    out_path = sys.argv[1] if len(sys.argv) > 1 else "dist/The_Tessera_Manifesto.pdf"
    build_pdf(out_path)
