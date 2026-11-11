from pathlib import Path
from xml.sax.saxutils import escape

from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER, TA_LEFT
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import inch
from reportlab.pdfbase.pdfmetrics import stringWidth
from reportlab.platypus import (
    Flowable,
    Image,
    KeepTogether,
    PageBreak,
    Paragraph,
    SimpleDocTemplate,
    Spacer,
    Table,
    TableStyle,
)

ROOT = Path(__file__).resolve().parent
PDF_PATH = ROOT / "CineVerse-Report.pdf"

INK = colors.HexColor("#17212B")
DARK = colors.HexColor("#11171F")
RED = colors.HexColor("#E9424F")
GOLD = colors.HexColor("#D89B48")
TEAL = colors.HexColor("#287F83")
MUTED = colors.HexColor("#64717D")
PALE = colors.HexColor("#F3F5F7")
LINE = colors.HexColor("#D9DEE3")
WHITE = colors.white

styles = getSampleStyleSheet()
styles.add(ParagraphStyle(
    name="ReportTitle", parent=styles["Title"], fontName="Helvetica-Bold",
    fontSize=25, leading=31, textColor=INK, alignment=TA_LEFT, spaceAfter=8,
))
styles.add(ParagraphStyle(
    name="Section", parent=styles["Heading1"], fontName="Helvetica-Bold",
    fontSize=16, leading=20, textColor=INK, spaceBefore=10, spaceAfter=7,
    keepWithNext=True,
))
styles.add(ParagraphStyle(
    name="Subsection", parent=styles["Heading2"], fontName="Helvetica-Bold",
    fontSize=11.5, leading=15, textColor=TEAL, spaceBefore=7, spaceAfter=4,
    keepWithNext=True,
))
styles.add(ParagraphStyle(
    name="ReportBody", parent=styles["BodyText"], fontName="Helvetica",
    fontSize=9.4, leading=14, textColor=INK, spaceAfter=6,
))
styles.add(ParagraphStyle(
    name="Small", parent=styles["BodyText"], fontName="Helvetica",
    fontSize=8, leading=11, textColor=MUTED, spaceAfter=4,
))
styles.add(ParagraphStyle(
    name="Caption", parent=styles["BodyText"], fontName="Helvetica-Oblique",
    fontSize=8.2, leading=11, alignment=TA_CENTER, textColor=MUTED,
    spaceBefore=5, spaceAfter=10,
))
styles.add(ParagraphStyle(
    name="TableHead", parent=styles["BodyText"], fontName="Helvetica-Bold",
    fontSize=8, leading=10, textColor=WHITE,
))
styles.add(ParagraphStyle(
    name="TableCell", parent=styles["BodyText"], fontName="Helvetica",
    fontSize=7.8, leading=10, textColor=INK,
))
styles.add(ParagraphStyle(
    name="Callout", parent=styles["BodyText"], fontName="Helvetica-Bold",
    fontSize=9, leading=13, textColor=INK,
))


def para(text, style="ReportBody"):
    return Paragraph(escape(text), styles[style])


def bullet_list(items):
    rows = []
    for item in items:
        rows.append([Paragraph("&#8226;", styles["ReportBody"]), para(item)])
    table = Table(rows, colWidths=[15, 475], hAlign="LEFT")
    table.setStyle(TableStyle([
        ("VALIGN", (0, 0), (-1, -1), "TOP"),
        ("LEFTPADDING", (0, 0), (-1, -1), 0),
        ("RIGHTPADDING", (0, 0), (-1, -1), 3),
        ("TOPPADDING", (0, 0), (-1, -1), 1),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 1),
    ]))
    return table


def section(title, text=None):
    result = [Paragraph(escape(title), styles["Section"])]
    if text:
        result.append(para(text))
    return result


def callout(label, text):
    table = Table([[para(label, "Callout"), para(text, "ReportBody")]], colWidths=[95, 395])
    table.setStyle(TableStyle([
        ("BACKGROUND", (0, 0), (-1, -1), PALE),
        ("BOX", (0, 0), (-1, -1), 0.6, LINE),
        ("LINEBEFORE", (0, 0), (0, -1), 3, RED),
        ("VALIGN", (0, 0), (-1, -1), "TOP"),
        ("LEFTPADDING", (0, 0), (-1, -1), 9),
        ("RIGHTPADDING", (0, 0), (-1, -1), 9),
        ("TOPPADDING", (0, 0), (-1, -1), 7),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 4),
    ]))
    return table


def styled_table(headers, rows, widths):
    data = [[para(cell, "TableHead") for cell in headers]]
    data.extend([[para(str(cell), "TableCell") for cell in row] for row in rows])
    table = Table(data, colWidths=widths, repeatRows=1, hAlign="LEFT")
    table.setStyle(TableStyle([
        ("BACKGROUND", (0, 0), (-1, 0), DARK),
        ("ROWBACKGROUNDS", (0, 1), (-1, -1), [WHITE, PALE]),
        ("GRID", (0, 0), (-1, -1), 0.35, LINE),
        ("VALIGN", (0, 0), (-1, -1), "TOP"),
        ("LEFTPADDING", (0, 0), (-1, -1), 6),
        ("RIGHTPADDING", (0, 0), (-1, -1), 6),
        ("TOPPADDING", (0, 0), (-1, -1), 6),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 6),
    ]))
    return table


class ArchitectureDiagram(Flowable):
    def __init__(self):
        super().__init__()
        self.width = 500
        self.height = 190

    def draw(self):
        c = self.canv
        boxes = [
            (12, 118, 142, 48, "CUSTOMER / ADMIN", "Browser UI", TEAL),
            (178, 118, 142, 48, "SPRING BOOT", "Controllers + REST API", RED),
            (344, 118, 142, 48, "APPLICATION LOGIC", "Services + validation", GOLD),
            (178, 34, 142, 48, "JPA / REPOSITORIES", "Persistence boundary", TEAL),
            (344, 34, 142, 48, "H2 DATABASE", "In-memory demo data", DARK),
        ]
        for x, y, w, h, title, detail, color in boxes:
            c.setFillColor(WHITE)
            c.setStrokeColor(LINE)
            c.roundRect(x, y, w, h, 6, fill=1, stroke=1)
            c.setFillColor(color)
            c.roundRect(x, y + h - 5, w, 5, 2, fill=1, stroke=0)
            c.setFillColor(INK)
            c.setFont("Helvetica-Bold", 8)
            c.drawCentredString(x + w / 2, y + 29, title)
            c.setFillColor(MUTED)
            c.setFont("Helvetica", 7.5)
            c.drawCentredString(x + w / 2, y + 14, detail)
        self._arrow(154, 142, 178, 142, "HTTPS / JSON")
        self._arrow(320, 142, 344, 142, "service call")
        self._arrow(415, 118, 415, 82, "repository access")
        self._arrow(344, 58, 320, 58, "JPA")
        self._arrow(250, 118, 250, 82, "queries / updates")
        c.setFillColor(MUTED)
        c.setFont("Helvetica-Oblique", 7)
        c.drawString(12, 12, "Logical view of the current application; external payment processing is not implemented.")

    def _arrow(self, x1, y1, x2, y2, label):
        c = self.canv
        c.setStrokeColor(MUTED)
        c.setFillColor(MUTED)
        c.setLineWidth(0.9)
        c.line(x1, y1, x2, y2)
        if abs(x2 - x1) >= abs(y2 - y1):
            direction = 1 if x2 >= x1 else -1
            c.line(x2, y2, x2 - direction * 5, y2 + 3)
            c.line(x2, y2, x2 - direction * 5, y2 - 3)
            c.setFont("Helvetica", 6.3)
            c.drawCentredString((x1 + x2) / 2, y1 + 5, label)
        else:
            direction = 1 if y2 >= y1 else -1
            c.line(x2, y2, x2 - 3, y2 - direction * 5)
            c.line(x2, y2, x2 + 3, y2 - direction * 5)
            c.saveState()
            c.translate(x1 + 4, (y1 + y2) / 2)
            c.rotate(90)
            c.setFont("Helvetica", 6.3)
            c.drawCentredString(0, 0, label)
            c.restoreState()


class WorkflowDiagram(Flowable):
    def __init__(self):
        super().__init__()
        self.width = 500
        self.height = 105

    def draw(self):
        c = self.canv
        labels = ["Sign in", "Choose city\n& movie", "Select cinema\n& show", "Choose seats\n+ food", "Review total\n& confirm", "Ticket /\nMy Bookings"]
        x0, y, w, h, gap = 4, 37, 74, 42, 9
        for i, label in enumerate(labels):
            x = x0 + i * (w + gap)
            c.setFillColor(WHITE)
            c.setStrokeColor(LINE)
            c.roundRect(x, y, w, h, 5, fill=1, stroke=1)
            c.setFillColor(RED if i in (0, 4, 5) else TEAL)
            c.circle(x + 11, y + h - 10, 4, fill=1, stroke=0)
            c.setFillColor(INK)
            c.setFont("Helvetica-Bold", 7)
            for j, line in enumerate(label.split("\n")):
                c.drawCentredString(x + w / 2, y + 23 - j * 10, line)
            if i < len(labels) - 1:
                x1 = x + w
                x2 = x + w + gap
                c.setStrokeColor(MUTED)
                c.line(x1 + 1, y + h / 2, x2 - 2, y + h / 2)
                c.line(x2 - 2, y + h / 2, x2 - 6, y + h / 2 + 3)
                c.line(x2 - 2, y + h / 2, x2 - 6, y + h / 2 - 3)
        c.setFillColor(MUTED)
        c.setFont("Helvetica", 7)
        c.drawString(4, 18, "Admin path: separate admin sign-in → dashboard → report filters / booking list / show pricing.")


class DataModelDiagram(Flowable):
    def __init__(self):
        super().__init__()
        self.width = 500
        self.height = 245

    def draw(self):
        c = self.canv
        boxes = {
            "CustomerAccount": (12, 170, 112, 42, "CustomerAccount", "1 account"),
            "Booking": (190, 170, 112, 42, "Booking", "status, totals"),
            "Show": (368, 170, 112, 42, "Show", "time, price"),
            "BookingSeat": (98, 88, 112, 42, "BookingSeat", "booking-seat link"),
            "BookingFoodItem": (278, 88, 112, 42, "BookingFoodItem", "item, qty, price"),
            "Seat": (12, 12, 112, 42, "Seat", "screen position"),
            "Movie": (190, 12, 112, 42, "Movie", "catalog entry"),
            "Screen / Cinema": (368, 12, 112, 42, "Screen / Cinema", "venue structure"),
        }
        for title, (x, y, w, h, name, detail) in boxes.items():
            c.setFillColor(WHITE)
            c.setStrokeColor(LINE)
            c.roundRect(x, y, w, h, 5, fill=1, stroke=1)
            c.setFillColor(RED if title in ("Booking", "BookingSeat") else TEAL)
            c.rect(x, y + h - 4, w, 4, fill=1, stroke=0)
            c.setFillColor(INK)
            c.setFont("Helvetica-Bold", 7.4)
            c.drawCentredString(x + w / 2, y + 24, name)
            c.setFillColor(MUTED)
            c.setFont("Helvetica", 6.8)
            c.drawCentredString(x + w / 2, y + 10, detail)
        self._link(124, 191, 190, 191, "0..*")
        self._link(302, 191, 368, 191, "1")
        self._link(225, 170, 154, 130, "1..*")
        self._link(255, 170, 334, 130, "0..*")
        self._link(124, 88, 68, 54, "1 seat")
        self._link(246, 170, 246, 54, "movie")
        self._link(424, 170, 424, 54, "screen")
        c.setFont("Helvetica-Oblique", 7)
        c.setFillColor(MUTED)
        c.drawString(12, 0, "Simplified domain model. Show references Movie and Screen; Screen belongs to a Cinema.")

    def _link(self, x1, y1, x2, y2, label):
        c = self.canv
        c.setStrokeColor(MUTED)
        c.setFillColor(MUTED)
        c.setLineWidth(0.8)
        c.line(x1, y1, x2, y2)
        c.setFont("Helvetica", 6.4)
        c.drawCentredString((x1 + x2) / 2, (y1 + y2) / 2 + 3, label)


class UmlUseCaseDiagram(Flowable):
    def __init__(self):
        super().__init__()
        self.width = 500
        self.height = 245

    def draw(self):
        c = self.canv
        c.setStrokeColor(MUTED)
        c.setFillColor(WHITE)
        c.roundRect(92, 18, 320, 215, 8, fill=1, stroke=1)
        c.setFillColor(INK)
        c.setFont("Helvetica-Bold", 8)
        c.drawCentredString(252, 220, "CINEVERSE SYSTEM")
        customer_cases = [
            (165, 190, "Create account / sign in"),
            (165, 150, "Browse movies"),
            (165, 110, "Choose city and show"),
            (165, 70, "Select seats and food"),
            (165, 34, "Confirm / view booking"),
        ]
        admin_cases = [
            (325, 170, "View dashboard / reports"),
            (325, 120, "Search reservations"),
            (325, 70, "Change show price"),
        ]
        for x, y, label in customer_cases + admin_cases:
            c.setFillColor(PALE)
            c.setStrokeColor(TEAL if x < 250 else RED)
            c.ellipse(x - 62, y - 13, x + 62, y + 13, fill=1, stroke=1)
            c.setFillColor(INK)
            c.setFont("Helvetica", 6.8)
            c.drawCentredString(x, y - 2, label)
        self._actor(37, 132, "Customer")
        self._actor(466, 132, "Admin")
        for _, y, _ in customer_cases:
            c.setStrokeColor(MUTED)
            c.setLineWidth(0.55)
            c.line(49, 142, 103, y)
        for _, y, _ in admin_cases:
            c.setStrokeColor(MUTED)
            c.setLineWidth(0.55)
            c.line(455, 142, 387, y)
        c.setFillColor(MUTED)
        c.setFont("Helvetica-Oblique", 6.8)
        c.drawString(96, 7, "Admin use cases require an ADMIN account; sign-in also checks the configured Admin ID.")

    def _actor(self, x, y, label):
        c = self.canv
        c.setStrokeColor(INK)
        c.setFillColor(INK)
        c.setLineWidth(1)
        c.circle(x, y + 19, 6, fill=0, stroke=1)
        c.line(x, y + 13, x, y - 7)
        c.line(x - 10, y + 5, x + 10, y + 5)
        c.line(x, y - 7, x - 9, y - 19)
        c.line(x, y - 7, x + 9, y - 19)
        c.setFont("Helvetica-Bold", 7)
        c.drawCentredString(x, y - 31, label)


class UmlClassDiagram(Flowable):
    def __init__(self):
        super().__init__()
        self.width = 500
        self.height = 282

    def draw(self):
        c = self.canv
        boxes = {
            "CustomerAccount": (5, 216, "id, email, role", "createdAt"),
            "Booking": (178, 216, "reference, status", "seatSubtotal, foodSubtotal, total"),
            "Show": (351, 216, "startTime, endTime", "ticketPrice, status"),
            "BookingSeat": (5, 132, "booking, show", "seat"),
            "BookingFoodItem": (178, 132, "itemId, name", "quantity, unitPrice"),
            "Movie": (351, 132, "title, genre", "language, duration, rating"),
            "Seat": (5, 48, "rowLabel, seatNumber", "screen"),
            "Screen": (178, 48, "name, capacity", "cinema"),
            "Cinema": (351, 48, "name, city", "address, facilities"),
        }
        self._line(150, 244, 178, 244, "0..1       0..*")
        self._line(323, 244, 351, 244, "0..*       1")
        self._line(220, 216, 125, 188, "1       1..*")
        self._line(250, 216, 250, 188, "1       0..*")
        self._line(423, 216, 423, 188, "0..*       1")
        self._line(400, 216, 265, 104, "0..* shows")
        self._line(78, 132, 78, 104, "0..*       1")
        self._line(150, 76, 178, 76, "1       1..*")
        self._line(323, 76, 351, 76, "1       1..*")
        for name, (x, y, attributes, details) in boxes.items():
            w, h = 145, 56
            c.setFillColor(WHITE)
            c.setStrokeColor(LINE)
            c.rect(x, y, w, h, fill=1, stroke=1)
            c.setFillColor(TEAL if name not in ("Booking", "BookingSeat") else RED)
            c.rect(x, y + h - 17, w, 17, fill=1, stroke=0)
            c.setFillColor(WHITE)
            c.setFont("Helvetica-Bold", 7.3)
            c.drawCentredString(x + w / 2, y + h - 12, name)
            c.setFillColor(INK)
            c.setFont("Helvetica", 6.7)
            c.drawString(x + 5, y + 25, "+ " + attributes)
            c.drawString(x + 5, y + 12, "+ " + details)
        c.setFillColor(MUTED)
        c.setFont("Helvetica-Oblique", 6.6)
        c.drawString(5, 20, "Simplified UML domain class view; DTOs, repositories and service classes are omitted for readability.")

    def _line(self, x1, y1, x2, y2, label):
        c = self.canv
        c.setStrokeColor(MUTED)
        c.setFillColor(MUTED)
        c.setLineWidth(0.65)
        c.line(x1, y1, x2, y2)
        c.setFont("Helvetica", 5.8)
        c.drawCentredString((x1 + x2) / 2, (y1 + y2) / 2 + 3, label)


class UmlSequenceDiagram(Flowable):
    def __init__(self):
        super().__init__()
        self.width = 500
        self.height = 305

    def draw(self):
        c = self.canv
        xs = [28, 112, 204, 298, 394, 482]
        names = ["Customer", "Browser", "Booking\nController", "Booking\nService", "Repository", "H2 DB"]
        for x, name in zip(xs, names):
            c.setFillColor(WHITE)
            c.setStrokeColor(LINE)
            c.roundRect(x - 35, 270, 70, 27, 4, fill=1, stroke=1)
            c.setFillColor(INK)
            c.setFont("Helvetica-Bold", 6.6)
            for idx, line in enumerate(name.split("\n")):
                c.drawCentredString(x, 281 - idx * 8, line)
            c.setStrokeColor(MUTED)
            c.setDash(2, 2)
            c.line(x, 270, x, 12)
            c.setDash()
        messages = [
            (0, 1, "submit booking choices", False),
            (1, 2, "POST /api/bookings", False),
            (2, 3, "createBooking(request)", False),
            (3, 4, "load show, seats, menu", False),
            (4, 5, "query data", False),
            (5, 4, "show and seat data", True),
            (3, 4, "save booking + lines", False),
            (4, 5, "persist records", False),
            (5, 4, "commit", True),
            (3, 2, "booking DTO", True),
            (2, 1, "201 Created + summary", True),
            (1, 0, "display confirmation", False),
        ]
        y = 250
        for source, target, label, dashed in messages:
            x1, x2 = xs[source], xs[target]
            c.setStrokeColor(RED if source in (2, 3) else TEAL)
            c.setFillColor(INK)
            c.setLineWidth(0.8)
            if dashed:
                c.setDash(3, 2)
            c.line(x1, y, x2, y)
            c.setDash()
            direction = 1 if x2 > x1 else -1
            c.line(x2, y, x2 - direction * 5, y + 3)
            c.line(x2, y, x2 - direction * 5, y - 3)
            c.setFont("Helvetica", 5.9)
            c.drawCentredString((x1 + x2) / 2, y + 3, label)
            y -= 19


class UmlActivityDiagram(Flowable):
    def __init__(self):
        super().__init__()
        self.width = 500
        self.height = 325

    def draw(self):
        c = self.canv
        self._start(250, 300)
        self._activity(180, 258, 140, 30, "Sign in / create account")
        self._activity(180, 214, 140, 30, "Choose city, movie and date")
        self._activity(180, 170, 140, 30, "Load configured / demo shows")
        self._activity(180, 126, 140, 30, "Select show, seats and food")
        self._diamond(250, 88, 104, 42, "Request valid?\nSeats available?")
        self._activity(180, 35, 140, 30, "Calculate totals and save booking")
        self._final(250, 12)
        self._arrow(250, 294, 250, 288)
        self._arrow(250, 258, 250, 244)
        self._arrow(250, 214, 250, 200)
        self._arrow(250, 170, 250, 156)
        self._arrow(250, 126, 250, 109)
        self._arrow(250, 67, 250, 65)
        self._arrow(250, 35, 250, 23)
        self._activity(16, 75, 120, 34, "Show validation error; revise selection")
        c.setStrokeColor(RED)
        c.setFillColor(RED)
        c.setFont("Helvetica-Bold", 6.5)
        c.drawString(314, 91, "Yes")
        c.drawString(143, 99, "No")
        c.setStrokeColor(MUTED)
        c.line(198, 88, 150, 88)
        c.line(150, 88, 150, 92)
        c.line(150, 92, 136, 92)
        c.line(136, 92, 141, 95)
        c.line(136, 92, 141, 89)
        c.line(76, 109, 76, 143)
        c.line(76, 143, 180, 143)
        c.line(180, 143, 175, 146)
        c.line(180, 143, 175, 140)
        c.setFillColor(MUTED)
        c.setFont("Helvetica", 6.2)
        c.drawString(82, 147, "revise")
        c.setFillColor(MUTED)
        c.setFont("Helvetica-Oblique", 6.5)
        c.drawString(5, 2, "Activity flow for a demo booking; payment processing is not part of this flow.")

    def _start(self, x, y):
        c = self.canv
        c.setFillColor(INK)
        c.circle(x, y, 5, fill=1, stroke=0)

    def _final(self, x, y):
        c = self.canv
        c.setStrokeColor(INK)
        c.setFillColor(INK)
        c.circle(x, y, 7, fill=0, stroke=1)
        c.circle(x, y, 4, fill=1, stroke=0)

    def _activity(self, x, y, w, h, label):
        c = self.canv
        c.setFillColor(PALE)
        c.setStrokeColor(TEAL)
        c.roundRect(x, y, w, h, 8, fill=1, stroke=1)
        c.setFillColor(INK)
        c.setFont("Helvetica", 6.7)
        c.drawCentredString(x + w / 2, y + h / 2 - 2, label)

    def _diamond(self, x, y, w, h, label):
        c = self.canv
        p = c.beginPath()
        p.moveTo(x, y + h / 2)
        p.lineTo(x + w / 2, y)
        p.lineTo(x, y - h / 2)
        p.lineTo(x - w / 2, y)
        p.close()
        c.setFillColor(WHITE)
        c.setStrokeColor(GOLD)
        c.drawPath(p, fill=1, stroke=1)
        c.setFillColor(INK)
        c.setFont("Helvetica", 6.3)
        for i, line in enumerate(label.split("\n")):
            c.drawCentredString(x, y + 3 - i * 8, line)

    def _arrow(self, x1, y1, x2, y2):
        c = self.canv
        c.setStrokeColor(MUTED)
        c.line(x1, y1, x2, y2)
        c.line(x2, y2, x2 - 3, y2 + 5)
        c.line(x2, y2, x2 + 3, y2 + 5)


class UmlBookingStateDiagram(Flowable):
    def __init__(self):
        super().__init__()
        self.width = 500
        self.height = 155

    def draw(self):
        c = self.canv
        c.setFillColor(INK)
        c.circle(42, 94, 6, fill=1, stroke=0)
        self._state(126, 74, 112, 40, "CONFIRMED")
        self._state(309, 74, 112, 40, "CANCELLED")
        c.setStrokeColor(MUTED)
        c.line(48, 94, 126, 94)
        c.line(238, 94, 309, 94)
        c.line(309, 94, 304, 97)
        c.line(309, 94, 304, 91)
        c.setFillColor(INK)
        c.setFont("Helvetica", 6.6)
        c.drawCentredString(274, 101, "cancel(reason)")
        c.setStrokeColor(MUTED)
        c.line(421, 94, 462, 94)
        c.circle(470, 94, 8, fill=0, stroke=1)
        c.circle(470, 94, 5, fill=1, stroke=0)
        c.setFillColor(MUTED)
        c.setFont("Helvetica", 6.8)
        c.drawString(14, 33, "A successful booking is created as CONFIRMED; cancellation changes it to CANCELLED and stores reason/time.")
        c.drawString(14, 18, "Aftercare is a separate field: requesting it sets aftercareStatus = PENDING; it is not a BookingStatus state.")

    def _state(self, x, y, w, h, label):
        c = self.canv
        c.setFillColor(PALE)
        c.setStrokeColor(RED if label == "CANCELLED" else TEAL)
        c.roundRect(x, y, w, h, 12, fill=1, stroke=1)
        c.setFillColor(INK)
        c.setFont("Helvetica-Bold", 8)
        c.drawCentredString(x + w / 2, y + 15, label)


def screenshot(filename, title, caption, explanation):
    path = ROOT / filename
    if not path.exists():
        return [para(f"Screenshot not found: {filename}", "Small")]
    image = Image(str(path))
    max_w, max_h = 6.9 * inch, 4.25 * inch
    scale = min(max_w / image.imageWidth, max_h / image.imageHeight)
    image.drawWidth = image.imageWidth * scale
    image.drawHeight = image.imageHeight * scale
    image.hAlign = "CENTER"
    return [
        Paragraph(escape(title), styles["Subsection"]),
        para(explanation),
        image,
        para(caption, "Caption"),
    ]


def on_page(canvas, doc):
    canvas.saveState()
    page = canvas.getPageNumber()
    width, height = A4
    if page == 1:
        canvas.setFillColor(DARK)
        canvas.rect(0, 0, width, height, fill=1, stroke=0)
        canvas.setFillColor(RED)
        canvas.rect(0, 0, 17, height, fill=1, stroke=0)
        canvas.setFillColor(colors.HexColor("#19232D"))
        canvas.circle(width - 55, height - 90, 125, fill=1, stroke=0)
        canvas.setFillColor(colors.HexColor("#202A32"))
        canvas.circle(width - 105, height - 155, 74, fill=1, stroke=0)
        canvas.setStrokeColor(colors.HexColor("#34404A"))
        canvas.setLineWidth(0.8)
        canvas.line(55, 116, width - 55, 116)
        canvas.setFillColor(RED)
        canvas.setFont("Helvetica-Bold", 10)
        canvas.drawString(58, height - 112, "SOFTWARE ENGINEERING PROJECT REPORT")
        canvas.setFillColor(WHITE)
        canvas.setFont("Helvetica-Bold", 31)
        canvas.drawString(58, height - 175, "CineVerse")
        canvas.setFont("Helvetica", 18)
        canvas.drawString(58, height - 205, "Movie Ticket Booking System")
        canvas.setFillColor(colors.HexColor("#C8D0D7"))
        canvas.setFont("Helvetica", 10)
        canvas.drawString(58, height - 234, "Requirements • Design • Implementation • Testing")
        canvas.setFillColor(GOLD)
        canvas.setFont("Helvetica-Bold", 9)
        canvas.drawString(58, 91, "PREPARED BY")
        canvas.setFillColor(WHITE)
        canvas.setFont("Helvetica", 10)
        canvas.drawString(58, 73, "[Add team member names and student IDs]")
        canvas.setFillColor(GOLD)
        canvas.setFont("Helvetica-Bold", 9)
        canvas.drawString(58, 48, "SUBMITTED TO")
        canvas.setFillColor(WHITE)
        canvas.setFont("Helvetica", 10)
        canvas.drawString(58, 30, "[Add faculty name, course, department and college]")
        canvas.setFillColor(colors.HexColor("#C8D0D7"))
        canvas.setFont("Helvetica", 8)
        canvas.drawRightString(width - 38, 30, "2026")
    else:
        canvas.setFillColor(RED)
        canvas.rect(0, height - 9, width, 9, fill=1, stroke=0)
        canvas.setFillColor(MUTED)
        canvas.setFont("Helvetica-Bold", 7.5)
        canvas.drawString(0.68 * inch, height - 0.42 * inch, "CINEVERSE  /  PROJECT REPORT")
        canvas.setStrokeColor(LINE)
        canvas.setLineWidth(0.5)
        canvas.line(0.68 * inch, height - 0.5 * inch, width - 0.68 * inch, height - 0.5 * inch)
        canvas.line(0.68 * inch, 0.52 * inch, width - 0.68 * inch, 0.52 * inch)
        canvas.setFillColor(MUTED)
        canvas.setFont("Helvetica", 7.5)
        canvas.drawString(0.68 * inch, 0.34 * inch, "CineVerse • Demo application • No real payment processing")
        canvas.drawRightString(width - 0.68 * inch, 0.34 * inch, str(page - 1))
    canvas.restoreState()


story = [Spacer(1, 1), PageBreak()]
story += section("Project at a glance")
story.append(para(
    "CineVerse is a full-stack classroom prototype for discovering movies and completing a simulated cinema booking. "
    "It combines a static JavaScript frontend with a Spring Boot REST backend and JPA persistence. Customers can choose a city, "
    "movie, show, seats and concession items; the backend validates booking requests and saves a booking summary. An authorized "
    "administrator can review booking statistics and screening prices."
))
story.append(callout("Important scope note", "Cinema listings and payment are demo/simulated in the supplied screenshots. The displayed QR reference is illustrative, not scannable; no real payment is processed."))
story.append(Spacer(1, 10))
story += section("Abstract", "This report documents the analysis, design, implementation and verification of CineVerse, a movie ticket booking web application. The system demonstrates a complete customer flow from account access through movie and show selection, seat and food selection, billing and ticket display. Its admin module provides report summaries, reservation search and screening-price management. The active development configuration uses an H2 in-memory database, so demo data is recreated when the application restarts. The report includes the system architecture, workflow, simplified domain model, implementation evidence, test coverage and project limitations.")
story += section("Contents")
story.append(bullet_list([
    "1. Introduction and problem definition",
    "2. Objectives, scope and stakeholders",
    "3. Requirements specification",
    "4. Architecture and UML-style design views",
    "5. Data design and business rules",
    "6. Implementation and user-interface evidence",
    "7. Verification and test coverage",
    "8. Challenges, limitations and future work",
    "9. Conclusion and references",
]))

story += section("1. Introduction")
story.append(para("Online movie booking involves more than selecting a title: users need a clear route through location, screening, seat availability and order totals. CineVerse models that journey in a browser-based application. The backend owns important decisions such as account roles, booking validation, seat reservation and totals, while the frontend presents the workflow and current selection."))
story += section("1.1 Problem statement", "Users need a straightforward way to find a movie and screening for a selected city, understand seat and food charges before confirming, and retrieve their booking afterwards. Administrators need protected access to booking summaries and screening prices. The project addresses these requirements with a guided customer workflow and a role-protected reporting module. Demo theatre inventory is explicitly labelled where used.")
story += section("1.2 Objectives")
story.append(bullet_list([
    "Provide customer registration, login and account-aware booking access.",
    "Support city, movie, cinema and show selection.",
    "Display seat availability and apply premium-seat pricing rules.",
    "Include concession items in the booking total.",
    "Persist booking details and provide a confirmation/ticket view.",
    "Restrict administration functions to an authenticated ADMIN role and configured Admin ID.",
    "Demonstrate maintainable separation between web controllers, services, repositories and entities.",
]))
story += section("1.3 Scope and constraints")
story.append(bullet_list([
    "In scope: customer accounts, movie discovery/catalog, booking shows, seats, food orders, booking aftercare, admin reporting and screening-price management.",
    "Demo constraint: the default datasource is H2 in-memory and uses create-drop schema behavior; records do not persist across application restarts.",
    "Demo constraint: payment status is simulated; there is no payment processor or real financial transaction.",
    "Demo constraint: fallback cinemas and showtimes are labelled as demo data and must not be presented as real theatre availability.",
    "The current application configuration selects H2; a PostgreSQL driver is also declared in Maven, but a PostgreSQL datasource is not active in application.properties.",
]))
story += section("1.4 Stakeholders")
story.append(styled_table(
    ["Stakeholder", "Goal / responsibility"],
    [
        ("Customer", "Find a film and show, reserve seats, add snacks, review and retrieve a booking."),
        ("Administrator", "Review booking reports and update screening prices using protected access."),
        ("Project team", "Analyze requirements, implement, test, document and maintain the prototype."),
        ("Faculty / evaluator", "Assess requirements, design decisions, implementation evidence and testing."),
    ], [1.35 * inch, 5.45 * inch]
))

story += section("2. Requirements specification")
story.append(Paragraph("2.1 Functional requirements", styles["Subsection"]))
story.append(styled_table(
    ["ID", "Requirement"],
    [
        ("FR-01", "A visitor can register a customer account and sign in."),
        ("FR-02", "A customer can select a city and browse movies and movie details."),
        ("FR-03", "A customer can request available cinemas and showtimes for a movie, city and date."),
        ("FR-04", "A customer can inspect seat availability and select seats for a show."),
        ("FR-05", "The booking service calculates seat and food subtotals and the total server-side."),
        ("FR-06", "A customer can confirm a demo booking and retrieve its ticket/booking summary."),
        ("FR-07", "A customer can view their own bookings and submit eligible aftercare requests."),
        ("FR-08", "An admin must pass admin ID, email, password and ADMIN role checks."),
        ("FR-09", "An admin can view filtered dashboard reports and reservation data."),
        ("FR-10", "An admin can update a screening's ticket price; changes apply to future bookings."),
    ], [0.65 * inch, 6.15 * inch]
))
story.append(Paragraph("2.2 Non-functional requirements", styles["Subsection"]))
story.append(bullet_list([
    "Security: password hashes are stored; admin functions require an ADMIN role; state-changing requests use CSRF verification.",
    "Integrity: the backend validates booking data and prevents duplicate reservations for the same show and seat.",
    "Usability: the interface presents the booking as sequential, visible steps and explains simulated inventory/payment.",
    "Maintainability: controllers, services, repositories, DTOs and JPA entities are separated by responsibility.",
    "Portability for development: Maven and an embedded H2 database avoid requiring a separately installed database server.",
]))

story += section("3. Architecture and design")
story.append(para("The application follows a client-server, layered design. The browser renders the static frontend and calls REST endpoints. Controllers validate/route HTTP requests, services apply booking and access rules, repositories use Spring Data JPA, and entity classes map the domain to relational tables. The current development profile stores data in H2 in-memory."))
story.append(Spacer(1, 5))
story.append(ArchitectureDiagram())
story.append(para("Figure 1. Logical application architecture based on the project structure and active datasource configuration.", "Caption"))
story.append(Paragraph("3.1 UML use-case diagram", styles["Subsection"]))
story.append(UmlUseCaseDiagram())
story.append(para("UML U1. Customer and administrator use cases within the CineVerse system boundary.", "Caption"))
story.append(Paragraph("3.2 Customer booking workflow", styles["Subsection"]))
story.append(WorkflowDiagram())
story.append(para("Figure 2. Customer booking workflow. The admin path is separate and guarded by role and Admin ID checks.", "Caption"))
story.append(Paragraph("3.3 Booking sequence", styles["Subsection"]))
story.append(styled_table(
    ["Step", "Interaction", "Responsibility"],
    [
        ("1", "Customer selects a show, seats, food and payment method.", "Frontend collects the requested choices."),
        ("2", "Frontend sends a booking request to the REST API.", "Controller validates request shape and session."),
        ("3", "Booking service loads show/seats and checks availability.", "Service enforces booking rules and computes authoritative prices."),
        ("4", "JPA persists booking, seats and food lines.", "Repositories write to the configured H2 datasource."),
        ("5", "API returns booking DTO; UI renders confirmation.", "Frontend displays reference, subtotals and simulated payment status."),
    ], [0.45 * inch, 3.0 * inch, 3.35 * inch]
))
story.append(UmlSequenceDiagram())
story.append(para("UML U3. Sequence diagram for booking creation, server-side validation, persistence and response. Payment is simulated.", "Caption"))
story.append(Paragraph("3.4 Booking activity diagram", styles["Subsection"]))
story.append(UmlActivityDiagram())
story.append(para("UML U4. Activity diagram with the validation/seat-availability decision and correction path.", "Caption"))
story.append(Paragraph("3.5 Booking state diagram", styles["Subsection"]))
story.append(UmlBookingStateDiagram())
story.append(para("UML U5. Booking lifecycle states implemented by BookingStatus.", "Caption"))

story += section("4. Data design")
story.append(para("JPA entities represent customers, movies, cinemas, screens, seats, shows and bookings. BookingSeat links a booking to a seat for a specific show; BookingFoodItem records the purchased item name, quantity and unit price. Booking stores seat subtotal, food subtotal, total, status and optional aftercare details. The diagram is intentionally simplified and omits implementation fields that are not needed to understand the relationships."))
story.append(Paragraph("4.1 UML class diagram", styles["Subsection"]))
story.append(UmlClassDiagram())
story.append(para("UML U2. Core domain classes, selected attributes and multiplicities based on the JPA entities.", "Caption"))
story.append(DataModelDiagram())
story.append(para("Figure 4. Simplified domain/entity relationship view. Cinema owns screens; each show references a movie and a screen.", "Caption"))
story.append(Paragraph("4.2 Key business rules", styles["Subsection"]))
story.append(bullet_list([
    "A booking belongs to one show and may contain several booked seats and food lines.",
    "A database uniqueness constraint prevents the same seat from being reserved twice for one show.",
    "The booking service calculates totals using server-side show pricing and the allowed food menu; the browser is not authoritative for price.",
    "Premium seats are identified by the configured front-row rule; standard seats use the base show price.",
    "Cancelling a booking changes its status and records a reason/time; aftercare requests have their own type and status.",
    "The Admin dashboard is restricted to the ADMIN account role; the configured Admin ID is also checked during admin sign-in.",
]))
story.append(Paragraph("4.3 Main data entities", styles["Subsection"]))
story.append(styled_table(
    ["Entity", "Purpose"],
    [
        ("CustomerAccount", "Customer identity, encoded password and account role."),
        ("Movie", "Movie catalog and details shown during discovery/booking."),
        ("Cinema / Screen / Seat", "Venue hierarchy and physical seating layout."),
        ("Show", "Scheduled movie on a screen with time, status and ticket price."),
        ("Booking", "Reservation reference, customer/show, status, subtotals and total."),
        ("BookingSeat", "Join entity connecting a booking, show and reserved seat."),
        ("BookingFoodItem", "Food order line with item ID, name, quantity and unit price."),
    ], [1.7 * inch, 5.1 * inch]
))

story += section("5. Technology and implementation")
story.append(styled_table(
    ["Layer", "Technology / implementation"],
    [
        ("Frontend", "Static HTML, CSS and JavaScript served by Spring Boot."),
        ("Backend", "Java 17, Spring Boot 3.5.7, REST controllers and service layer."),
        ("Persistence", "Spring Data JPA / Hibernate with H2 in-memory datasource in application.properties."),
        ("Build", "Maven; Spring Boot Maven plugin."),
        ("Verification", "JUnit 5, Spring Boot Test and MockMvc integration tests."),
        ("Version control", "Git repository hosted on GitHub."),
    ], [1.45 * inch, 5.35 * inch]
))
story.append(Paragraph("5.1 Object-oriented and design concepts", styles["Subsection"]))
story.append(bullet_list([
    "Encapsulation: entity state is private and accessed or changed through methods, keeping persistence objects responsible for their data.",
    "Abstraction: controllers work with DTOs and services rather than exposing JPA entities directly to the browser.",
    "Association: the domain models relationships such as Cinema–Screen, Show–Movie, Show–Screen, Booking–Show, Booking–BookingSeat and Booking–BookingFoodItem.",
    "Single responsibility / layered design: controllers handle HTTP, services enforce business rules, repositories handle persistence, and DTOs define API payloads.",
    "Enums: account role, booking status, show status and aftercare states constrain domain values to explicit options.",
    "Validation and defensive checks: request validation, session/role checks and server-side price calculation protect key operations.",
]))
story.append(Paragraph("5.2 Booking total calculation", styles["Subsection"]))
story.append(callout("Total", "Booking total = server-calculated seat subtotal + server-calculated food subtotal. The checkout screen shows payment choices, but completing the booking records a simulated result rather than charging a payment method."))
story.append(Paragraph("Project planning and management", styles["Section"]))
story.append(callout("Plan status", "The schedule and role split below are a proposed planning template, not a claim about the team's actual historical dates or individual contributions. Replace the role placeholders with your group's real details."))
story.append(styled_table(
    ["Phase", "Suggested duration", "Work and deliverables"],
    [
        ("1. Requirements", "Week 1", "Confirm problem, users, scope, functional/non-functional requirements and acceptance criteria."),
        ("2. Analysis and design", "Week 2", "Review use cases, UML models, architecture, entity relationships and UI flow."),
        ("3. Implementation", "Weeks 3-4", "Implement frontend, Spring Boot APIs, booking rules, persistence, admin functions and integration."),
        ("4. Verification", "Week 5", "Run unit/integration/system tests, record defects, fix issues and repeat regression tests."),
        ("5. Documentation and demo", "Week 5", "Prepare screenshots, report, presentation, run instructions and demonstration checklist."),
    ], [1.25 * inch, 1.0 * inch, 4.55 * inch]
))
story.append(Paragraph("Suggested responsibilities for a group of 2-3", styles["Subsection"]))
story.append(styled_table(
    ["Role placeholder", "Suggested ownership"],
    [
        ("Member A: [name / ID]", "Requirements, UML, documentation and coordination."),
        ("Member B: [name / ID]", "Backend entities/services/APIs, persistence and backend tests."),
        ("Member C: [name / ID, if applicable]", "Frontend workflow, UI integration, screenshots and demo preparation."),
        ("Shared", "Design reviews, integration testing, defect triage and final presentation."),
    ], [1.8 * inch, 5.0 * inch]
))

story += section("6. User-interface evidence")
story.append(para("The following figures are captures from the supplied running application. They document the customer journey and administration interface. City listings and sample cinema inventory shown in the captures are demo data. Personal signup/profile screenshots were intentionally omitted from this report to avoid publishing the user's email address."))
story.append(PageBreak())
story += screenshot(
    "Screenshot 2026-11-09 200318.png", "6.1 Home and featured movie", 
    "Figure 5. CineVerse homepage with featured film, navigation and city selector.",
    "The home page introduces the movie discovery experience and provides navigation to movies, bookings and account actions."
)
story.append(PageBreak())
story += screenshot(
    "Screenshot 2026-11-09 200615.png", "6.2 Movie details", 
    "Figure 6. Movie details page with film information and booking entry point.",
    "The movie page presents title details and gives the user a direct route into the booking flow."
)
story.append(PageBreak())
story += screenshot(
    "Screenshot 2026-11-09 200746.png", "6.3 City and show-date selection", 
    "Figure 7. Show-date selection with the active city displayed.",
    "The booking flow keeps the selected city visible while the customer chooses an available show date."
)
story.append(PageBreak())
story += screenshot(
    "Screenshot 2026-11-09 200817.png", "6.4 Cinema and showtime choices", 
    "Figure 8. Example city results with three clearly marked demo cinema options.",
    "This capture shows the cinema grouping and showtime cards. These locations are labelled DEMO and represent simulated inventory, not real theatres."
)
story.append(PageBreak())
story += screenshot(
    "Screenshot 2026-11-09 200920.png", "6.5 Seat and food selection", 
    "Figure 9. Seat layout with premium/standard legend, snack quantities and live total.",
    "The selection view distinguishes front-row premium seats from standard rows and includes concession quantities in the displayed amount."
)
story.append(PageBreak())
story += screenshot(
    "Screenshot 2026-11-09 200941.png", "6.6 Checkout review", 
    "Figure 10. Simulated checkout showing seat subtotal, food subtotal and total due.",
    "The review page itemizes the booking before confirmation. Payment methods shown are demonstration choices only."
)
story.append(PageBreak())
story += screenshot(
    "Screenshot 2026-11-09 201105.png", "6.7 Booking confirmation", 
    "Figure 11. Confirmed demo booking with seats, subtotals and illustrative reference.",
    "The booking detail view presents the saved demo reservation and its server-returned totals. The reference/QR is explicitly illustrative and not scannable."
)
story.append(PageBreak())
story += screenshot(
    "Screenshot 2026-11-09 201450.png", "6.8 Admin dashboard overview", 
    "Figure 12. Admin dashboard summary metrics and date filters.",
    "The administrator view summarizes catalog, cinema, show, account, booking and demo-revenue measures. Values are from the captured demo dataset, not production statistics."
)
story.append(PageBreak())
story += screenshot(
    "Screenshot 2026-11-09 201505.png", "6.9 Reports and reservations", 
    "Figure 13. Admin booking breakdowns and reservation report table.",
    "The report view includes grouped booking metrics, payment/cancellation summaries, reservation search and CSV export controls."
)

story.append(PageBreak())
story += section("7. Verification and testing")
story.append(para("The repository includes automated Spring Boot/MockMvc tests for core booking and administrator behavior. The following results are from an actual `mvn test` run on 10 Nov 2026."))
story.append(styled_table(
    ["Area", "Tested behavior in source", "Evidence file"],
    [
        ("Booking", "Create a confirmed booking, calculate totals, verify seat availability and retrieve the booking.", "BookingControllerTest"),
        ("Pricing / food", "Check premium and standard seat totals, food menu and persisted food line totals.", "BookingControllerTest"),
        ("Show fallback", "Use configured shows first and return at least three explicitly flagged demo choices when needed.", "BookingControllerTest"),
        ("Admin authentication", "Reject customer role, wrong admin ID and admin login through the customer endpoint; accept configured admin credentials.", "AdminControllerTest"),
        ("Authorization / CSRF", "Reject anonymous/customer report access and reject state-changing requests without a valid CSRF token.", "AdminControllerTest"),
        ("Admin reporting", "Filter dashboard data by show date and reject invalid date ranges.", "AdminControllerTest"),
    ], [1.05 * inch, 4.65 * inch, 1.1 * inch]
))
story.append(Spacer(1, 8))
story.append(callout("Actual test run", "Command: `mvn test` (10 Nov 2026). Result: 52 tests, 49 passed, 3 failed, 0 errors, 0 skipped; Maven reported BUILD FAILURE. These are observed assertion mismatches; root causes have not yet been diagnosed."))
story.append(styled_table(
    ["Failing test", "Observed assertion"],
    [
        ("AdminControllerTest.adminReportIsRoleProtectedAndFiltersBookingsByShowDate", "Expected totalRevenue 350.0; received 437.5."),
        ("AuthControllerTest.customerBookingReportAndBookingLookupAreRestrictedToOwner", "Expected HTTP 201; received HTTP 400."),
        ("CinemaShowControllerTest.developmentDataLoaderIsIdempotent", "Expected 3 records; found 10."),
    ], [4.1 * inch, 2.7 * inch]
))
story.append(Spacer(1, 6))
story.append(para("All 15 BookingControllerTest cases passed in this run. Other passing suite counts: DiscoveryControllerTest 3/3, HealthControllerTest 1/1 and MovieControllerTest 12/12. AdminControllerTest and AuthControllerTest each had 3 passed and 1 failed; CinemaShowControllerTest had 12 passed and 1 failed.", "Small"))
story += section("7.1 Sample manual acceptance checks")
story.append(styled_table(
    ["Check", "Expected observation"],
    [
        ("Customer flow", "City, movie, date, cinema and show are carried forward through seat selection."),
        ("Seat selection", "Selected seats appear in the summary; occupied seats are unavailable."),
        ("Food calculation", "Changing food quantity changes the food subtotal and total."),
        ("Booking confirmation", "Confirmation displays show, seats, food, subtotals and demo payment status."),
        ("Admin boundary", "Customer accounts cannot access admin report or price management endpoints."),
    ], [1.5 * inch, 5.3 * inch]
))

story += section("8. Challenges, limitations and future work")
story.append(Paragraph("8.1 Challenges and design responses", styles["Subsection"]))
story.append(styled_table(
    ["Challenge", "Response in the project"],
    [
        ("Preserving user context across booking/auth steps", "Carry the selected city and return route through the frontend flow."),
        ("Avoiding client-controlled totals", "Calculate seat/food totals in the backend and return authoritative booking values."),
        ("Preventing duplicate seat sales", "Validate availability and enforce a database uniqueness constraint per show/seat."),
        ("Providing theatre choices with sparse local inventory", "Use clearly labelled demo cinema/show records where suitable real configured data is absent."),
        ("Protecting admin operations", "Require the ADMIN role, configured Admin ID and CSRF verification for protected actions."),
    ], [2.4 * inch, 4.4 * inch]
))
story.append(Paragraph("8.2 Limitations", styles["Subsection"]))
story.append(bullet_list([
    "H2 is in-memory under the current configuration; data resets after application restart.",
    "The payment flow is simulated; no card, UPI or wallet transaction occurs.",
    "Some cinema/show data is demonstration inventory, not live theatre availability.",
    "The booking reference QR in the current UI is explicitly illustrative and not a scannable admission credential.",
    "The report screenshots show one demo dataset and should not be interpreted as live operational analytics.",
]))
story.append(Paragraph("8.3 Maintenance plan", styles["Subsection"]))
story.append(styled_table(
    ["When", "Maintenance activity"],
    [
        ("For every code change", "Run focused tests and `mvn test`; review validation, authorization and booking-total behavior; update related documentation."),
        ("Each release", "Build from a clean checkout, verify environment configuration, review logs and confirm demo/production labels."),
        ("Monthly or before a submission", "Review Spring Boot/Maven dependency updates, security advisories, open defects and UI compatibility."),
        ("Before production use", "Replace in-memory H2 with persistent storage, add migrations/backups, protect secrets, configure monitoring and integrate real payment processing."),
        ("After an incident", "Reproduce the issue, preserve diagnostic logs without secrets, add a regression test, fix and re-verify."),
    ], [1.55 * inch, 5.25 * inch]
))
story.append(para("Assign a maintainer for each release. Admin credentials and API keys must remain in environment/configuration secrets and must not be added to source control.", "Small"))
story.append(Paragraph("8.4 Future enhancements", styles["Subsection"]))
story.append(bullet_list([
    "Move to a persistent production database and define migration/backup procedures.",
    "Integrate a compliant payment provider with secure transaction status handling.",
    "Issue verifiable QR tickets and add entry validation at the venue.",
    "Connect to live cinema/showtime inventory and notify users of booking changes.",
    "Add email/SMS notifications, ratings, accessibility options and richer analytics.",
]))

story += section("9. Conclusion")
story.append(para("CineVerse demonstrates a complete movie reservation workflow and applies software-engineering practices across requirements, modular design, persistence, access control, pricing and testing. The project supports city and movie discovery, show selection, seat and food choices, server-calculated booking totals, ticket summaries and admin reporting. Its current H2 and payment configuration is intentionally suitable for a classroom demo, not a live ticketing service. The included test sources and visual evidence provide a basis for evaluating the implementation, while persistent storage, live inventory and real payment processing remain clear future extensions."))
story += section("References")
story.append(bullet_list([
    "Spring Boot Reference Documentation, https://docs.spring.io/spring-boot/",
    "Spring Data JPA Reference Documentation, https://docs.spring.io/spring-data/jpa/",
    "H2 Database Documentation, https://www.h2database.com/",
    "Java Platform Documentation, https://docs.oracle.com/en/java/",
    "CineVerse source code, project README and automated test sources.",
]))
story += section("Appendix A. Run the project")
story.append(para("From the project root, run `mvn spring-boot:run`. The default port is 8080, configurable with the PORT environment variable. The development datasource is embedded H2, so no separate database installation is required. For a submission demonstration, use only demo accounts and sample data; do not include passwords, Admin IDs or private environment values in the report."))
story.append(Spacer(1, 12))
story.append(callout("Before submission", "Fill in the cover-page team/faculty/college fields, replace proposed planning placeholders with your group's real details, and review/fix the three failing tests before claiming the suite passes. Review screenshots for private information before sharing the PDF."))

doc = SimpleDocTemplate(
    str(PDF_PATH), pagesize=A4,
    rightMargin=0.68 * inch, leftMargin=0.68 * inch,
    topMargin=0.72 * inch, bottomMargin=0.72 * inch,
    title="CineVerse Movie Ticket Booking System - Project Report",
    author="CineVerse Project Team",
    subject="Software Engineering Project Report",
)
doc.build(story, onFirstPage=on_page, onLaterPages=on_page)
print(f"Generated {PDF_PATH}")
