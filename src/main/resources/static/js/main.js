console.log('gameCompleted:', window.gameData.gameCompleted);
console.log('gameWon:', window.gameData.gameWon);

let currentTile = 0;
let currentGuess = '';
const maxAttempts = window.gameData.maxAttempts;
const wordLength = window.gameData.wordLength;
const sessionId = window.gameData.sessionId;
const gameCompleted = window.gameData.gameCompleted;
const gameWon = window.gameData.gameWon;
const previousGuesses = window.gameData.previousGuesses;
const letterStates = {};
let currentRow = previousGuesses ? previousGuesses.length : 0;

if (previousGuesses && previousGuesses.length > 0) {
    previousGuesses.forEach((guessResult, rowIndex) => {
        const isLastRow = rowIndex === previousGuesses.length - 1;
        const tiles = document.querySelectorAll(`.board-row:nth-child(${rowIndex + 1}) .tile`);
        const word = guessResult.guess || guessResult.word;
        const feedback = guessResult.feedback;

        for (let i = 0; i < word.length; i++) {
            const tile = tiles[i];
            tile.textContent = word[i];
            tile.classList.add('filled');

            const feedbackChar = feedback[i];
            const colorClass = feedbackChar === 'C' ? 'correct' : feedbackChar === 'P' ? 'present' : 'absent';

            if (isLastRow) {
                const delay = (1 - Math.pow((word.length - i) / word.length, 1.5)) * 800;
                setTimeout(() => {
                    tile.classList.add('flip');
                    setTimeout(() => {
                        tile.classList.add(colorClass);
                        if (feedbackChar === 'C') {
                            letterStates[word[i]] = 'correct';
                        } else if (feedbackChar === 'P') {
                            if (letterStates[word[i]] !== 'correct') letterStates[word[i]] = 'present';
                        } else {
                            if (!letterStates[word[i]]) letterStates[word[i]] = 'absent';
                        }
                    }, 250);
                }, delay);
            } else {
                tile.classList.add(colorClass);
                if (feedbackChar === 'C') {
                    letterStates[word[i]] = 'correct';
                } else if (feedbackChar === 'P') {
                    if (letterStates[word[i]] !== 'correct') letterStates[word[i]] = 'present';
                } else {
                    if (!letterStates[word[i]]) letterStates[word[i]] = 'absent';
                }
            }
        }
    });
    currentRow = previousGuesses.length;
    document.getElementById('current-attempt').textContent = currentRow;
    updateKeyboard();
    if (previousGuesses.length > 0) {
        setTimeout(() => updateKeyboard(), wordLength * 300 + 500);
    }
}

if (gameCompleted) {
    setTimeout(() => showGameOverModal(), 1700);
}

document.querySelectorAll('.key').forEach(key => {
    key.addEventListener('click', () => {
        handleInput(key.dataset.key);
    });
});

document.addEventListener('keydown', (e) => {
    if (gameCompleted) return;
    if (e.key === 'Enter') {
        handleInput('ENTER');
    } else if (e.key === 'Backspace') {
        handleInput('BACKSPACE');
    } else if (e.key.match(/^[a-zA-Z]$/)) {
        handleInput(e.key.toUpperCase());
    }
});

function handleInput(key) {
    console.log("Handling input");
    console.log(currentRow);
    if (gameCompleted) return;
    if (key === 'ENTER') {
        submitGuess();
    } else if (key === 'BACKSPACE') {
        deleteLetter();
    } else if (currentTile < wordLength) {
        addLetter(key);
    }
}

function addLetter(letter) {
    if (currentTile < wordLength) {
        const tile = document.querySelector(
            `.board-row:nth-child(${currentRow + 1}) .tile:nth-child(${currentTile + 1})`
        );
        tile.textContent = letter;
        tile.classList.add('filled');
        currentGuess += letter;
        currentTile++;
    }
}

function deleteLetter() {
    if (currentTile > 0) {
        currentTile--;
        const tile = document.querySelector(
            `.board-row:nth-child(${currentRow + 1}) .tile:nth-child(${currentTile + 1})`
        );
        tile.textContent = '';
        tile.classList.remove('filled');
        currentGuess = currentGuess.slice(0, -1);
    }
}

function submitGuess() {
    if (currentGuess.length !== wordLength) {
        alert('Not enough letters!');
        return;
    }
    console.log(currentGuess);
    document.getElementById('guess-input').value = currentGuess;
    document.getElementById('guess-form').submit();
}

function updateKeyboard() {
    Object.keys(letterStates).forEach(letter => {
        const key = document.querySelector(`[data-key="${letter}"]`);
        if (key) {
            key.classList.remove('correct', 'present', 'absent');
            key.classList.add(letterStates[letter]);
        }
    });
}

function showGameOverModal() {
    const modal = document.getElementById('game-over-modal');
    const title = document.getElementById('modal-title');
    const message = document.getElementById('modal-message');

    if (gameWon) {
        title.textContent = 'You won!';
        message.textContent = 'The word was: ' + window.gameData.answer;
        triggerConfetti();
    } else {
        title.textContent = 'Game Over!';
        message.textContent = 'The word was: ' + window.gameData.answer;
        triggerShake();
    }
    modal.classList.add('show');
}

function triggerConfetti() {
    confetti({
        particleCount: 150,
        spread: 70,
        origin: { y: 0.6 }
    });
}

function triggerShake() {
    const board = document.getElementById('game-board');
    board.classList.add('shake');
    board.addEventListener('animationend', () => {
        board.classList.remove('shake');
    }, { once: true });
}