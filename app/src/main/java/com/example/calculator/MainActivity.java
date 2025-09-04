package com.example.calculator;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.calculator.databinding.ActivityMainBinding;

import org.mariuszgromada.math.mxparser.Expression;


public class MainActivity extends AppCompatActivity {

    ActivityMainBinding mainBinding;

    // Stores the current input string (numbers/operators)
    String number = null;

    // To keep track of parentheses counts for balancing
    int cntOpenPar = 0;
    int cntClosePar = 0;

    // Flags to control user input
    boolean operator = false;         // true = last pressed was an operator
    boolean dotControl = false;       // true = dot already exists in current number
    boolean buttonEqualsControl = false; // true = last pressed was '='

    // Stores the evaluated result string
    String result = "";

    // SharedPreferences to persist app state (theme + calculator history)
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // 🔹 Load theme preference before inflating UI
        prefs = this.getSharedPreferences("com.example.calculator", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("switch", false);

        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);

        // 🔹 Binding replaces findViewById
        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());

        // Initial display
        mainBinding.textViewresult.setText("0");

        // 🔹 All button click listeners
        mainBinding.btn0.setOnClickListener(v -> onNumberClicked("0"));
        mainBinding.btn1.setOnClickListener(v -> onNumberClicked("1"));
        mainBinding.btn2.setOnClickListener(v -> onNumberClicked("2"));
        mainBinding.btn3.setOnClickListener(v -> onNumberClicked("3"));
        mainBinding.btn4.setOnClickListener(v -> onNumberClicked("4"));
        mainBinding.btn5.setOnClickListener(v -> onNumberClicked("5"));
        mainBinding.btn6.setOnClickListener(v -> onNumberClicked("6"));
        mainBinding.btn7.setOnClickListener(v -> onNumberClicked("7"));
        mainBinding.btn8.setOnClickListener(v -> onNumberClicked("8"));
        mainBinding.btn9.setOnClickListener(v -> onNumberClicked("9"));

        // Parentheses
        mainBinding.btnOpen.setOnClickListener(v -> {
            onParClicked("(");
            cntOpenPar++;
        });
        mainBinding.btnclose.setOnClickListener(v -> {
            if(cntOpenPar > cntClosePar){
                onParClicked(")");
                cntClosePar++;
            }
        });

        // Operators (+, -, *, /)
        mainBinding.btnPlus.setOnClickListener(v -> onOperatorClicked("+"));
        mainBinding.btnMinus.setOnClickListener(v -> onOperatorClicked("-"));
        mainBinding.btnMulti.setOnClickListener(v -> onOperatorClicked("*"));
        mainBinding.btnDivide.setOnClickListener(v -> onOperatorClicked("/"));

        // Delete button
        mainBinding.btnDel.setOnClickListener(v -> onDeleteClicked());

        // AC button (clear all)
        mainBinding.btnAC.setOnClickListener(v -> onACClicked());

        // Decimal dot
        mainBinding.btnDot.setOnClickListener(v -> onDotClicked());

        // Equal button (=)
        mainBinding.btnEqual.setOnClickListener(v -> onEqualClicked());

        // Toolbar → Settings menu (theme change screen)
        mainBinding.toolbar.setOnMenuItemClickListener(item -> {
            if(item.getItemId() == R.id.settingsitem){
                Intent intent = new Intent(MainActivity.this, ChangeThemeActivity.class);
                startActivity(intent);
                return true;
            }else {
                return false;
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        // 🔹 Save calculator state into SharedPreferences
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("resultText", mainBinding.textViewresult.getText().toString());
        editor.putString("history", mainBinding.textViewhistory.getText().toString());
        editor.putString("result", result);
        editor.putString("number", number);
        editor.putBoolean("operator", operator);
        editor.putBoolean("dot", dotControl);
        editor.putBoolean("equal", buttonEqualsControl);
        editor.putInt("countOpenPar", cntOpenPar);
        editor.putInt("countClosePar", cntClosePar);

        editor.apply();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // 🔹 Restore calculator state when activity restarts
        mainBinding.textViewresult.setText(prefs.getString("resultText","0"));
        mainBinding.textViewhistory.setText(prefs.getString("history",""));
        result = prefs.getString("result","");
        number = prefs.getString("number",null);
        operator = prefs.getBoolean("operator",false);
        dotControl = prefs.getBoolean("dot",false);
        buttonEqualsControl = prefs.getBoolean("equal",false);
        cntOpenPar = prefs.getInt("countOpenPar",0);
        cntClosePar = prefs.getInt("countClosePar",0);
    }

    // 🔹 Handles digit button clicks
    public void onNumberClicked(String clickedNumber){
        if(number == null || buttonEqualsControl){
            number = clickedNumber;
        }
        else{
            number += clickedNumber;
        }

        mainBinding.textViewresult.setText(number);
        operator = false;
        dotControl = false;
        buttonEqualsControl = false;
    }

    // 🔹 Handles parentheses button clicks
    public void onParClicked(String Par){
        if(number==null || buttonEqualsControl){
            number = Par;
        }
        else{
            number += Par;
        }

        mainBinding.textViewresult.setText(number);
        buttonEqualsControl = false;
    }

    // 🔹 Handles AC button
    public void onACClicked(){
        number = null;
        mainBinding.textViewresult.setText("0");
        mainBinding.textViewhistory.setText("");
        dotControl = false;
        operator = false;
        cntClosePar = 0;
        cntOpenPar = 0;
        buttonEqualsControl = false;
        result = "";
    }

    // 🔹 Handles operator buttons
    public void onOperatorClicked(String op){
        if(!operator && !dotControl){
            if(number == null){
                number = "0" + op;
            }else if(buttonEqualsControl){
                number = result + op;
            }
            else{
                number += op;
            }
            mainBinding.textViewresult.setText(number);
            operator = true;
            dotControl = true;
            buttonEqualsControl = false;
        }
    }

    // 🔹 Handles delete button
    public void onDeleteClicked(){
        if(number==null || number.length()==0){
            onACClicked();
        }
        else{
            String lastChar = String.valueOf(number.charAt(number.length()-1));

            // Adjust flags and parentheses counts when deleting
            switch(lastChar){
                case"+" :case"-" :case"*" : case"/" : case"." :
                    operator = false;
                    dotControl = false;
                    break;
                case"(" :
                    cntOpenPar--;
                    break;
                case")" :
                    cntClosePar--;
                    break;
            }

            // Remove last char
            number = number.substring(0,number.length()-1);

            if (number.isEmpty()) {
                onACClicked();
                return;
            }

            // Recheck last char to restore flags
            lastChar = String.valueOf(number.charAt(number.length()-1));
            switch(lastChar){
                case"+" :case"-" :case"*" : case"/" : case"." :
                    operator = true;
                    dotControl = true;
                    break;
            }

            mainBinding.textViewresult.setText(number);
        }
    }

    // 🔹 Handles decimal dot button
    public void onDotClicked(){
        if(!dotControl && !operator){
            if(buttonEqualsControl){
                if(!result.contains(".")){
                    number = result + ".";
                    mainBinding.textViewresult.setText(number);
                    dotControl = true;
                    buttonEqualsControl = false;
                }
            }
            else{
                if(number == null){
                    number = "0.";
                    mainBinding.textViewresult.setText(number);
                    dotControl = true;
                    operator = true;
                }
                else{
                    // Extract substring after last operator to prevent multiple dots in same number
                    String expressionAfterLastOperator = "";
                    String lastCharacter;
                    doLoop: for(int i=number.length()-1; i>=0; i--){
                        lastCharacter = String.valueOf(number.charAt(i));
                        switch (lastCharacter){
                            case"+": case"-": case"*": case"/":
                                break doLoop;
                            default:
                                expressionAfterLastOperator = lastCharacter.concat(expressionAfterLastOperator);
                                break;
                        }
                    }

                    if(!expressionAfterLastOperator.contains("."))  {
                        number += ".";
                        mainBinding.textViewresult.setText(number);
                        dotControl = true;
                        operator = true;
                    }
                }
            }
        }
    }

    // 🔹 Handles equal button (=)
    public void onEqualClicked(){
        String expressionForCalculator = mainBinding.textViewresult.getText().toString();

        // Close any unclosed parentheses
        int difference = cntOpenPar-cntClosePar;
        if(difference>0){
            for(int i=0; i<difference; i++){
                expressionForCalculator = expressionForCalculator.concat(")");
            }
        }

        Expression expression = new Expression(expressionForCalculator);
        result = String.valueOf(expression.calculate());

        if(result.equals("NaN")){
            // Handle division by zero errors
            checkDivisor(expressionForCalculator);
        }else{
            // If result is integer, remove ".0"
            int indexofDot = result.indexOf(".");
            String expressionAfterDot = result.substring(indexofDot+1);
            if(expressionAfterDot.equals("0")){
                result = result.substring(0,indexofDot);
            }

            // Display result & history
            mainBinding.textViewresult.setText(result);
            mainBinding.textViewhistory.setText(expressionForCalculator);

            // Reset flags for next input
            buttonEqualsControl = true;
            operator = false;
            dotControl = false;
            cntClosePar = 0;
            cntOpenPar = 0;
        }
    }

    // 🔹 Checks divisor to prevent division by zero
    @SuppressLint("SetTextI18n")
    public void checkDivisor(String expressionForCalculate){
        if(expressionForCalculate.contains("/")){
            int indexOfSlash = expressionForCalculate.indexOf("/");
            String expressionAfterSlash = expressionForCalculate.substring(indexOfSlash+1);

            // Balance parentheses in divisor
            if(expressionAfterSlash.contains(")")){
                int closingPar = 0, openingPar = 0;
                for(int i=0; i<expressionAfterSlash.length(); i++){
                    String isPar = String.valueOf(expressionAfterSlash.charAt(i));
                    if(isPar.equals("(")){
                        openingPar++;
                    }else if(isPar.equals(")")){
                        closingPar++;
                    }
                }
                int diff = closingPar-openingPar;
                if(diff>0){
                    for(int i=0; i<diff; i++){
                        expressionAfterSlash = "(".concat(expressionAfterSlash);
                    }
                }
            }

            Expression expression = new Expression(expressionAfterSlash);
            String newresult =  String.valueOf(expression.calculate());

            if(newresult.equals("0.0")){
                mainBinding.textViewhistory.setText("The Divisor cannot be Zero");
            }else {
                checkDivisor(expressionAfterSlash);
            }
        }else {
            mainBinding.textViewresult.setText("Syntax error");
        }
    }
}
