# Timeback Scheduler – Lesson Management System

A web application for teachers to manage their weekly lesson timetables, featuring real-time notifications and a clean, modern interface.

---

## 🔥 What's Inside

* **Frontend**: Built with React 18, TypeScript, Vite, Tailwind CSS, and shadcn/ui components
* **Backend**: Developed with Spring Boot, featuring JWT authentication, role-based access control, and a fully documented API

---

## ⚡ Project Structure

```
timeback-scheduler/
├── frontend/     # React app
├── backend/      # Spring Boot API
└── README.md     # Project overview (this file)
```

---

## ✨ Getting Started

### Clone the project

```bash
git clone https://github.com/austinendlovu/Junior-Developer-Challange.git
cd teacher-time-bridge
```

---

### ✉ Email Setup (Gmail)

The backend uses Gmail SMTP to send real-time lesson notifications.

#### To enable this:

* Go to your Google Account
* Navigate to **Security > 2-Step Verification** and enable it
* Go to **App Passwords**
* Choose **Mail** and your device, then generate a password
* Copy the generated password

#### Update `application.yml` with these values:

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-generated-app-password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

**Important**: Never push your real credentials to GitHub.

---

### Run the Backend

```bash
cd backend

# Update application.properties or application.yml with your DB and email credentials

mvn clean install
mvn spring-boot:run
```

* Backend will be live at: [http://localhost:8080](http://localhost:8080)
* Swagger API Docs: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

### Run the Frontend

```bash
cd ../frontend
npm install
npm run dev
```

* Frontend will be live at: [http://localhost:8081](http://localhost:8081)

---

## 🌍 Deployment

* **Frontend**: Use Vercel, Netlify, or any static site hosting
* **Backend**: Host on a VPS, Railway, Heroku, or any cloud platform like AWS, Azure, or GCP

---

## 🤝 Contributing

* Fork the repository
* Create a new branch: `git checkout -b feature/your-feature`
* Commit and push your changes
* Open a Pull Request

---

## 📜 License

MIT License

---

## 📬 Support

For help, open an issue in the repository or contact the team directly.
