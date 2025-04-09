package com.example.quiz;

public class QuizResult {
    private int score;
    private String difficulty;
    private String date;

    public QuizResult(int score, String difficulty, String date) {
        this.score = score;
        this.difficulty = difficulty;
        this.date = date;
    }

    public int getScore() {
        return score;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getDate() {
        return date;
    }
}
