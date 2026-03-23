// Import required packages
const express = require('express');
const cors = require('cors');

// Create an Express application
const app = express();

// Server will run on port 5000
const PORT = 5000;

// Enable CORS so your Android app (or other clients) can call this API
app.use(cors());

// In-memory sample data (no database for now)
const games = [
  { id: 1, title: 'The Legend of Zelda: Breath of the Wild', genre: 'Action-Adventure', releaseYear: 2017 },
  { id: 2, title: 'Hades', genre: 'Roguelike', releaseYear: 2020 },
  { id: 3, title: 'Stardew Valley', genre: 'Simulation', releaseYear: 2016 },
  { id: 4, title: 'Elden Ring', genre: 'Action RPG', releaseYear: 2022 },
  { id: 5, title: 'Minecraft', genre: 'Sandbox', releaseYear: 2011 }
];

// Basic health/check route (optional but useful while testing)
app.get('/', (req, res) => {
  res.send('GameLog API is running. Try GET /games');
});

// GET /games endpoint
// Returns a JSON array of sample games
app.get('/games', (req, res) => {
  res.json(games);
});

// Start the server
app.listen(PORT, () => {
  console.log(`GameLog API server is running on http://localhost:${PORT}`);
});
