package com.example.quiz;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuestionFragment extends Fragment {
    private static final String ARG_DIFFICULTY = "difficulty";
    private static final int QUESTIONS_PER_GAME = 10;

    private CountDownTimer timer;
    private int currentQuestion = 0;
    private int score = 0;
    private List<Question> questions;
    private List<Button> answerButtons;
    private TextView questionText;
    private TextView scoreText;
    private NavigationListener navigationListener;
    private String difficulty;
    private String currentUsername;
    private boolean isHint5050Used = false;
    private boolean isHintFriendUsed = false;

    public static QuestionFragment newInstance(String difficulty) {
        if (difficulty == null || difficulty.isEmpty()) {
            throw new IllegalArgumentException("Difficulty cannot be null or empty");
        }
        QuestionFragment fragment = new QuestionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DIFFICULTY, difficulty);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof NavigationListener) {
            navigationListener = (NavigationListener) context;
        } else {
            throw new RuntimeException(context.toString() + " должен реализовать NavigationListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_question, container, false);

        if (getArguments() == null) {
            throw new IllegalStateException("Arguments cannot be null");
        }

        difficulty = getArguments().getString(ARG_DIFFICULTY);
        if (difficulty == null || difficulty.isEmpty()) {
            throw new IllegalStateException("Difficulty cannot be null or empty");
        }

        questions = loadQuestions(difficulty);
        if (questions == null || questions.isEmpty()) {
            Toast.makeText(getContext(), "Ошибка загрузки вопросов", Toast.LENGTH_SHORT).show();
            navigationListener.navigateToFragment(new MenuFragment(), true);
            return view;
        }

        Collections.shuffle(questions);
        questions = questions.subList(0, Math.min(questions.size(), QUESTIONS_PER_GAME));

        questionText = view.findViewById(R.id.question_text);
        scoreText = view.findViewById(R.id.score_text);
        
        answerButtons = new ArrayList<>();
        answerButtons.add(view.findViewById(R.id.answer1));
        answerButtons.add(view.findViewById(R.id.answer2));
        answerButtons.add(view.findViewById(R.id.answer3));
        answerButtons.add(view.findViewById(R.id.answer4));

        Button hint5050Button = view.findViewById(R.id.hint_5050);
        Button hintFriendButton = view.findViewById(R.id.hint_friend);

        hint5050Button.setOnClickListener(v -> useHint5050());
        hintFriendButton.setOnClickListener(v -> useHintFriend());

        for (Button button : answerButtons) {
            if (button != null) {
                button.setOnClickListener(v -> checkAnswer((Button) v));
            }
        }

        setupQuestion();
        startTimer(view.findViewById(R.id.timer));

        return view;
    }

    private List<Question> loadQuestions(String difficulty) {
        List<Question> questions = new ArrayList<>();
        if (getContext() == null) {
            return questions;
        }

        Resources res = getResources();
        String packageName = getContext().getPackageName();

        int questionsRes = res.getIdentifier(difficulty + "_questions", "array", packageName);
        int answersRes = res.getIdentifier(difficulty + "_answers", "array", packageName);

        if (questionsRes == 0 || answersRes == 0) {
            return questions;
        }

        String[] questionArray = res.getStringArray(questionsRes);
        String[] answerArray = res.getStringArray(answersRes);
        
        if (questionArray == null || answerArray == null || questionArray.length != answerArray.length) {
            return questions;
        }

        for (int i = 0; i < questionArray.length; i++) {
            int optionsRes = res.getIdentifier(
                    difficulty + "_options_" + (i + 1), "array", packageName);
            if (optionsRes == 0) {
                continue;
            }

            String[] options = res.getStringArray(optionsRes);
            if (options == null || options.length != 4) {
                continue;
            }
            
            List<String> optionsList = new ArrayList<>();
            Collections.addAll(optionsList, options);
            
            questions.add(new Question(
                    questionArray[i],
                    optionsList,
                    answerArray[i],
                    "", // hint can be added later
                    difficulty
            ));
        }
        return questions;
    }

    private void setupQuestion() {
        if (currentQuestion >= questions.size()) {
            endGame();
            return;
        }

        Question question = questions.get(currentQuestion);
        if (questionText != null) {
            questionText.setText(question.getQuestion());
        }
        if (scoreText != null) {
            scoreText.setText(getString(R.string.score, score));
        }

        List<String> options = new ArrayList<>(question.getOptions());
        Collections.shuffle(options);

        for (int i = 0; i < answerButtons.size(); i++) {
            Button button = answerButtons.get(i);
            if (button != null && i < options.size()) {
                button.setText(options.get(i));
                button.setEnabled(true);
                button.setAlpha(1.0f);
                button.setBackgroundResource(android.R.drawable.btn_default);
                button.setBackgroundTintList(getResources().getColorStateList(R.color.purple_200));
                button.clearAnimation();
            }
        }

        // Сбрасываем состояние подсказок для нового вопроса
        isHint5050Used = false;
        isHintFriendUsed = false;
    }

    private void startTimer(TextView timerView) {
        if (timer != null) {
            timer.cancel();
        }

        if (timerView == null) {
            return;
        }

        long timeInMillis;
        switch (difficulty) {
            case "easy":
                timeInMillis = 45000; // 45 секунд
                break;
            case "medium":
                timeInMillis = 30000; // 30 секунд
                break;
            case "hard":
                timeInMillis = 20000; // 20 секунд
                break;
            default:
                timeInMillis = 30000;
        }

        timer = new CountDownTimer(timeInMillis, 1000) {
            public void onTick(long millisUntilFinished) {
                if (timerView != null) {
                    timerView.setText(getString(R.string.time_left, millisUntilFinished / 1000));
                }
            }

            public void onFinish() {
                moveToNextQuestion();
            }
        }.start();
    }

    private void checkAnswer(Button selectedButton) {
        if (timer != null) {
            timer.cancel();
        }

        Question currentQ = questions.get(currentQuestion);
        
        // Отключаем все кнопки
        for (Button button : answerButtons) {
            if (button != null) {
                button.setEnabled(false);
                button.setTextColor(getResources().getColor(android.R.color.black));
            }
        }

        if (selectedButton.getText().toString().equals(currentQ.getCorrectAnswer())) {
            score += 10;
            selectedButton.setBackgroundTintList(getResources().getColorStateList(R.color.correct_answer));
            playCorrectAnimation(selectedButton);
        } else {
            selectedButton.setBackgroundTintList(getResources().getColorStateList(R.color.wrong_answer));
            playWrongAnimation(selectedButton);
            vibrate();
        }

        // Показываем правильный ответ
        for (Button button : answerButtons) {
            if (button != null && button.getText().toString().equals(currentQ.getCorrectAnswer())) {
                button.setBackgroundTintList(getResources().getColorStateList(R.color.correct_answer));
                playCorrectAnimation(button);
            }
        }

        // Задержка перед следующим вопросом
        selectedButton.postDelayed(this::moveToNextQuestion, 1500);
    }

    private void playCorrectAnimation(View view) {
        if (view == null || getContext() == null) {
            return;
        }
        Animation scaleUpAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.scale_up);
        scaleUpAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                Animation scaleDownAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.scale_down);
                view.startAnimation(scaleDownAnimation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        view.startAnimation(scaleUpAnimation);
    }

    private void playWrongAnimation(View view) {
        if (view == null || getContext() == null) {
            return;
        }
        Animation scaleUpAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.scale_up);
        scaleUpAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                Animation scaleDownAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.scale_down);
                view.startAnimation(scaleDownAnimation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        view.startAnimation(scaleUpAnimation);
    }

    private void moveToNextQuestion() {
        currentQuestion++;
        if (currentQuestion < questions.size()) {
            setupQuestion();
            View view = getView();
            if (view != null) {
                startTimer(view.findViewById(R.id.timer));
            }
        } else {
            endGame();
        }
    }

    private void endGame() {
        if (getActivity() != null && currentUsername != null && !currentUsername.isEmpty()) {
            DatabaseHelper db = new DatabaseHelper(getActivity());
            int userId = db.getUserId(currentUsername);
            if (userId >= 0) {
                db.saveQuizResult(userId, score, difficulty);
            }
            navigationListener.navigateToFragment(ResultFragment.newInstance(score), true);
        }
    }

    private void vibrate() {
        if (getActivity() == null) {
            return;
        }
        Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            }
        }
    }

    public void setCurrentUsername(String username) {
        this.currentUsername = username;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        navigationListener = null;
        if (timer != null) {
            timer.cancel();
        }
    }

    private void useHint5050() {
        if (isHint5050Used) {
            Toast.makeText(getContext(), R.string.hint_already_used, Toast.LENGTH_SHORT).show();
            return;
        }

        Question currentQ = questions.get(currentQuestion);
        String correctAnswer = currentQ.getCorrectAnswer();
        List<String> wrongAnswers = new ArrayList<>();
        
        // Собираем все неправильные ответы
        for (String option : currentQ.getOptions()) {
            if (!option.equals(correctAnswer)) {
                wrongAnswers.add(option);
            }
        }
        
        // Случайно выбираем два неправильных ответа
        Collections.shuffle(wrongAnswers);
        wrongAnswers = wrongAnswers.subList(0, 2);
        
        // Отключаем выбранные неправильные ответы
        for (Button button : answerButtons) {
            if (wrongAnswers.contains(button.getText().toString())) {
                button.setEnabled(false);
                button.setAlpha(0.5f);
            }
        }

        isHint5050Used = true;
        Button hint5050Button = getView().findViewById(R.id.hint_5050);
        if (hint5050Button != null) {
            hint5050Button.setEnabled(false);
            hint5050Button.setAlpha(0.5f);
        }
        Toast.makeText(getContext(), R.string.hint_5050_used, Toast.LENGTH_SHORT).show();
    }

    private void useHintFriend() {
        if (isHintFriendUsed) {
            Toast.makeText(getContext(), R.string.hint_already_used, Toast.LENGTH_SHORT).show();
            return;
        }

        Question currentQ = questions.get(currentQuestion);
        List<String> activeOptions = new ArrayList<>();
        
        // Собираем только активные варианты ответов
        for (Button button : answerButtons) {
            if (button.isEnabled()) {
                activeOptions.add(button.getText().toString());
            }
        }
        
        // Формируем текст для отправки
        StringBuilder shareText = new StringBuilder();
        shareText.append("Вопрос из викторины: ");
//        shareText.append(getString(R.string.share_question_title)).append("\n\n");
        shareText.append(currentQ.getQuestion()).append("\n");
        shareText.append("Варианты ответов:\n");
        
        for (int i = 0; i < activeOptions.size(); i++) {
            shareText.append(i + 1).append(". ").append(activeOptions.get(i)).append("\n");
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_question_title)));

        isHintFriendUsed = true;
        Button hintFriendButton = getView().findViewById(R.id.hint_friend);
        if (hintFriendButton != null) {
            hintFriendButton.setEnabled(false);
            hintFriendButton.setAlpha(0.5f);
        }
        Toast.makeText(getContext(), R.string.hint_friend_used, Toast.LENGTH_SHORT).show();
    }
}