# CineVerse Movie Ticket Booking System

## Abstract
CineVerse is a web-based movie ticket booking system developed to simplify the booking experience for users. The system allows customers to select a city, browse available movies, choose a showtime, reserve seats, add food items, and review the final total before booking. The project also includes an admin dashboard for reporting, monitoring bookings, and managing pricing summaries. The application is built using Java, Spring Boot, HTML, CSS, JavaScript, and an H2 in-memory database for demo use.

## 1. Introduction
In the modern digital era, movie ticket booking systems have become an essential part of entertainment services. Customers expect quick and convenient access to select cities, movies, showtimes, and seats without confusion. Many online booking systems also need strong admin support to monitor bookings, summarize revenue, and manage operations. CineVerse addresses these needs by providing a complete booking workflow with a modern user interface and a separate admin module.

## 2. Problem Statement
The existing booking process often becomes complicated when a user has to choose a city, movie, showtime, and seat arrangement while also understanding premium pricing and food charges. Additionally, administrators need a reliable way to monitor bookings and revenue. The system was designed to solve these challenges by simplifying the user journey and offering a dedicated management dashboard for admin users.

## 3. Objectives
- To develop an easy-to-use online movie ticket booking system.
- To allow city-based movie and show selection.
- To support seat selection with premium and standard pricing.
- To add a food ordering section and include it in the final bill.
- To provide secure login and signup flows.
- To create an admin dashboard for reports and booking summaries.
- To develop the system using scalable and maintainable software design.

## 4. Scope of the Project
The scope of this project includes user authentication, city and movie selection, show and cinema display, seat reservation, food ordering, final billing, booking confirmation, and admin reporting. The system is designed as a demo project and does not include real payment integration, production deployment, or live transaction processing.

## 5. Requirements Analysis
### 5.1 Functional Requirements
- User can sign up and log in.
- User can select a city.
- User can browse movies and view details.
- User can choose a showtime and cinema.
- User can select seats for booking.
- Premium seats should have a higher price than regular seats.
- User can add food items to the booking.
- The total amount should include both seat charges and food charges.
- User can confirm booking and view a ticket summary.
- Admin can log in with proper admin credentials.
- Admin can access booking and revenue reports.

### 5.2 Non-Functional Requirements
- The system should be user-friendly and responsive.
- The interface should be simple and visually clear.
- The system should provide validation for booking inputs.
- The project should be maintainable and modular.
- The frontend and backend should be separated cleanly.
- The system should provide secure sessions for login and admin access.

## 6. System Architecture
The CineVerse project follows a client-server architecture. The front end provides the user interface, while the backend handles requests, authentication, validation, and booking logic. The data is stored in an H2 in-memory database, which makes the project easy to run and test without external setup. The architecture separates concerns between presentation, business logic, and persistence, making the system easier to maintain and extend.

## 7. Technology Stack
- Java
- Spring Boot
- Maven
- HTML
- CSS
- JavaScript
- H2 Database
- GitHub

## 8. System Design
CineVerse is designed in a modular structure. Customers access the booking workflow through the frontend. When a user selects a city, movie, showtime, and seats, the backend validates the request and calculates the final amount. Food items are added as part of the booking, and the final total is presented to the user before confirmation. The admin module allows an authorized user to view booking reports and summaries.

## 9. Use Case and Workflow
A typical user workflow includes the following steps:
1. Sign up or log in
2. Select the city
3. Choose a movie
4. Select a showtime and cinema
5. Choose seats
6. Add food items
7. Review billing details
8. Confirm booking
9. View booking confirmation or ticket summary

The admin workflow includes login, dashboard access, and report viewing.

## 10. Database Design
The project uses H2 as an in-memory database. It stores important entities such as customer accounts, movies, cinemas, shows, screens, bookings, seats, and food details. The relationship is designed so that one booking can include multiple seats and selected food items, while each show belongs to a particular cinema and screen. This structure makes the data model suitable for a booking system and allows fast demo testing.

## 11. Implementation Details
The project was implemented using Spring Boot for backend development and a dark-themed web interface for frontend presentation. Booking logic includes city selection, show selection, seat validation, premium seat pricing, and total bill calculation. Food items are added dynamically and included in the total amount. The admin dashboard allows monitoring of bookings, counts, and revenue totals for the selected period.

## 12. Key Features
- City-based movie selection
- Movie and showtime selection
- Premium and standard seat categories
- Food add-on menu
- Bill calculation with seat + food totals
- My Bookings panel
- Admin dashboard and report generation
- Demo environment for classroom project use

## 13. Testing and Validation
The application was tested by validating each stage of the booking process. Login, signup, city flow, movie selection, seat reservation, and final billing were checked to ensure the application behaved correctly. Price logic was validated to confirm that premium seats and food items were reflected in the total amount. Admin access was also tested to ensure dashboard features remain restricted to authorized users.

## 14. Challenges and Solutions
A major challenge was maintaining the selected city during login flow. Another issue involved handling premium seat pricing and ensuring the final bill reflected both seats and food items. The project also required a realistic demo setup for cinema and show data. These challenges were resolved by updating the booking logic, backend validation, and frontend flow so the application remained consistent and user-friendly.

## 15. Future Enhancements
- Real payment gateway integration
- Persistent database instead of in-memory H2
- Real QR ticket generation
- Email and SMS notifications
- User ratings and reviews
- More advanced admin analytics
- Recommendation engine for movie suggestions

## 16. Conclusion
CineVerse is a successful demonstration of a movie ticket booking system that combines frontend design, backend logic, validation, and admin reporting. It covers essential software engineering concepts such as system design, modularity, and functional validation. The project is a strong example of how modern web applications can provide a smooth user experience while handling real-world business rules and reporting requirements.

## 17. References
- Spring Boot Documentation
- Java Programming Concepts
- HTML, CSS, and JavaScript documentation
- GitHub repository for project version control
- Software engineering course materials and project guidelines
