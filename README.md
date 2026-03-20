<p align="center">
  <img src="images/qwerdle-title.svg" alt="Qwerdle" />
</p>

A scalable full-stack NYT Games Wordle clone built as a capstone project for the Java Enterprise career track at [CodingNomads](https://codingnomads.com/) with dictionary API integration, unlimited random games, daily words synced for all players, player profiles, and stat-tracking. 

---

## Features
- **Daily Games** — A scheduled API call at midnight every day populates the Word of the Day, which is cached server-side and shared for all players and can only be played once per user, win or lose.


- **Unlimited Random Games** — Play as many games as you would like in a day, with a random word pulled from a hardcoded list each time the game is generated.


- **User Authentication** — Registration, login, and session management via Spring Security with BCrypt password encoding stored in a server-side SQL database; games can even be played anonymously


- **Stat-Tracking** — A detailed user profile page that keeps score for you, remembering your wins, losses, and streaks


- **User Experience** — Gameplay as expected of a Wordle clone: 
  - color-coded feedback of guessed letters revealed with flipping tile animation
  - sleek, dark user interface
  - input letters using the onscreen keyboard or your own physical one
  - subtle restart button
  - confetti animation on correct guess and shaking screen effect on loss
  - consistent dashboard element for app navigation


- **Dictionary Validation** — Player guesses are validated against a third-party dictionary API in real time to prevent wasted attempts on typos or unexpected words


- **Persistent Game States** — Daily and random game states are maintained independently per session using Spring's SessionAttributes annotation, allowing players to switch between modes without losing progress, even persisting across anonymous play into login

---

## Tech Stack
- **Backend** — Java 17, Spring Boot, Spring Security, Spring Data JPA
- **Frontend** — Thymeleaf, HTML/CSS, JavaScript
- **Database** — MySQL
- **Build Tool** — Gradle
- **External API** — [WordsAPI](https://www.wordsapi.com/) via RapidAPI
- **Deployment** — Tomcat 10 on AWS EC2