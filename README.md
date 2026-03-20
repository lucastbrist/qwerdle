<p align="center">
  <img src="images/qwerdle-title.svg" alt="Qwerdle" />
</p>

![A screenshot of the play page](images/play_screenshot.png)

A scalable full-stack NYT Games Wordle clone built as a capstone project for the Java Enterprise career track at [CodingNomads](https://codingnomads.com/) with dictionary API integration, unlimited random games, daily words synced for all players, player profiles, and stat-tracking. 

---

## Features
- **Daily Games** - A scheduled API call at midnight every day populates the Word of the Day, which is cached server-side and shared for all players and can only be played once per user, win or lose.
- **Unlimited Random Games** - Play as many games as you would like in a day, with a random word pulled from a hardcoded list each time the game is generated.
- **User Authentication** - Registration, login, and session management via Spring Security with BCrypt password encoding stored in a server-side SQL database
- **Stat-Tracking** - A detailed user profile page that keeps score for you, remembering your wins, losses, and streaks