package com.example.mallozziu1teamproyect;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.math.BigDecimal;

public class MainActivity extends AppCompatActivity {

    private EditText etNumber;
    private Spinner spPrecision;
    private TextView tvOriginal, tvFixedPoint, tvFixedPointNormalized, tvExponent, tvMantissa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etNumber = findViewById(R.id.et_number);
        spPrecision = findViewById(R.id.sp_precision);
        tvOriginal = findViewById(R.id.tv_original);
        tvFixedPoint = findViewById(R.id.tv_fixed_point);
        tvFixedPointNormalized = findViewById(R.id.tv_fixed_point_normalized);
        tvExponent = findViewById(R.id.tv_exponent);
        tvMantissa = findViewById(R.id.tv_mantissa);
        Button btnCalculate = findViewById(R.id.btn_calculate);

        btnCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculate();
            }
        });
    }

    private void calculate() {
        String numberInput = etNumber.getText().toString();

        if (!numberInput.isEmpty()) {
            try {
                String selectedPrecision = spPrecision.getSelectedItem().toString();

                switch (selectedPrecision) {
                    case "Simple Precision (32 bits)":
                        calculateSinglePrecision(Float.parseFloat(numberInput));
                        break;
                    case "Double Precision (64 bits)":
                        calculateDoublePrecision(Double.parseDouble(numberInput));
                        break;
                    case "Quadruple Precision (128 bits)":
                        calculateQuadruplePrecision(new BigDecimal(numberInput));
                        break;
                }
            } catch (NumberFormatException e) {
                tvOriginal.setText("Invalid input");
                clearResults();
            }
        } else {
            tvOriginal.setText("Please enter a number");
            clearResults();
        }
    }

    private void clearResults() {
        tvFixedPoint.setText("");
        tvFixedPointNormalized.setText("");
        tvExponent.setText("");
        tvMantissa.setText("");
    }

    private void calculateSinglePrecision(float originalNumber) {
        tvOriginal.setText(" " + originalNumber);

        // Convertir a punto fijo
        String fixedPoint = toFixedPointString(originalNumber, 23);
        tvFixedPoint.setText("  " + fixedPoint);

        // Normalización del número en punto fijo
        String[] normalized = normalizeBinary(fixedPoint);
        tvFixedPointNormalized.setText(" : " + normalized[0] + " x 2^" + normalized[1]);

        // Cálculo del exponente
        int exponent = calculateExponent(originalNumber);
        int skewedExponent = exponent + 127; // Sesgo para precisión simple
        tvExponent.setText(" Exponent = " + exponent + "; Skewed exponent = " + skewedExponent + " (In binary: " + String.format("%8s", Integer.toBinaryString(skewedExponent)).replace(' ', '0') + ")");

        // Cálculo de la mantisa
        String mantissa = calculateMantissa(normalized[0]);
        tvMantissa.setText(" " + mantissa);
    }

    private void calculateDoublePrecision(double originalNumber) {
        tvOriginal.setText(" " + originalNumber);

        // Convertir a punto fijo
        String fixedPoint = toFixedPointString(originalNumber, 52);
        tvFixedPoint.setText(" " + fixedPoint);

        // Normalización del número en punto fijo
        String[] normalized = normalizeBinary(fixedPoint);
        tvFixedPointNormalized.setText(" " + normalized[0] + " x 2^" + normalized[1]);

        // Cálculo del exponente
        int exponent = calculateExponent(originalNumber);
        int skewedExponent = exponent + 1023; // Sesgo para precisión doble
        tvExponent.setText(" Exponent = " + exponent + "; Skewed exponent = " + skewedExponent + " (In binary: " + String.format("%11s", Integer.toBinaryString(skewedExponent)).replace(' ', '0') + ")");

        // Cálculo de la mantisa
        String mantissa = calculateMantissa(originalNumber); // Aquí se utiliza el método de mantisa para precisión doble
        tvMantissa.setText(" " + mantissa);
    }

    private void calculateQuadruplePrecision(BigDecimal originalNumber) {
        tvOriginal.setText(" " + originalNumber);

        // Convertir a punto fijo
        String fixedPoint = toFixedPointString(originalNumber, 112); // Cuádruple precisión utiliza 112 bits para la parte fraccionaria
        tvFixedPoint.setText(" " + fixedPoint);

        // Normalización del número en punto fijo
        String[] normalized = normalizeBinary(fixedPoint);
        tvFixedPointNormalized.setText(" " + normalized[0] + " x 2^" + normalized[1]);

        // Cálculo del exponente
        int exponent = calculateExponent(originalNumber.doubleValue());
        int skewedExponent = exponent + 16383; // Sesgo para precisión cuádruple
        tvExponent.setText(" Exponent = " + exponent + "; Skewed exponent = " + skewedExponent + " (In binary: " + String.format("%15s", Integer.toBinaryString(skewedExponent)).replace(' ', '0') + ")");

        // Cálculo de la mantisa
        String mantissa = calculateMantissa(originalNumber); // Aquí se utiliza el método de mantisa para precisión cuádruple
        tvMantissa.setText(" " + mantissa);
    }

    private String toFixedPointString(float number, int fractionBits) {
        // Convertir a punto fijo multiplicando por 2^fractionBits
        int fixedPoint = Math.round(number * (1 << fractionBits));
        String binary = Integer.toBinaryString(fixedPoint);

        // Asegurarse de que tenga al menos (fractionBits + 1) bits
        while (binary.length() < (fractionBits + 1)) {
            binary = "0" + binary;
        }

        // Separar la parte entera y la parte fraccionaria
        String integerPart = binary.substring(0, binary.length() - fractionBits);
        String fractionalPart = binary.substring(binary.length() - fractionBits);

        // Formatear número en punto fijo
        return String.format("%s.%s", integerPart, fractionalPart);
    }

    private String toFixedPointString(double number, int fractionBits) {
        long fixedPoint = Math.round(number * (1L << fractionBits));
        String binary = Long.toBinaryString(fixedPoint);

        // Asegurarse de que tenga al menos (fractionBits + 1) bits
        while (binary.length() < (fractionBits + 1)) {
            binary = "0" + binary;
        }

        // Separar la parte entera y la parte fraccionaria
        String integerPart = binary.substring(0, binary.length() - fractionBits);
        String fractionalPart = binary.substring(binary.length() - fractionBits);

        // Formatear número en punto fijo
        return String.format("%s.%s", integerPart, fractionalPart);
    }

    private String toFixedPointString(BigDecimal number, int fractionBits) {
        BigDecimal fixedPoint = number.multiply(BigDecimal.valueOf(1L << fractionBits));
        String binary = fixedPoint.toBigInteger().toString(2);

        // Asegurarse de que tenga al menos (fractionBits + 1) bits
        while (binary.length() < (fractionBits + 1)) {
            binary = "0" + binary;
        }

        // Separar la parte entera y la parte fraccionaria
        String integerPart = binary.substring(0, binary.length() - fractionBits);
        String fractionalPart = binary.substring(binary.length() - fractionBits);

        // Formatear número en punto fijo
        return String.format("%s.%s", integerPart, fractionalPart);
    }

    private String[] normalizeBinary(String binary) {
        int index = binary.indexOf('.');
        String integerPart = binary.substring(0, index);
        String fractionalPart = binary.substring(index + 1);

        // Eliminar ceros a la izquierda en la parte entera
        while (integerPart.length() > 0 && integerPart.charAt(0) == '0') {
            integerPart = integerPart.substring(1);
        }

        // Normalizar si la parte entera es 0
        if (integerPart.isEmpty()) {
            integerPart = "0";
        }

        int exponent = integerPart.length() - 1;
        String normalized = integerPart + fractionalPart;

        // Eliminar el primer 1 en la mantisa
        if (normalized.length() > 0 && normalized.charAt(0) == '1') {
            normalized = normalized.substring(1);
        }

        return new String[]{normalized, String.valueOf(exponent)};
    }

    private int calculateExponent(double number) {
        return (int) Math.floor(Math.log(Math.abs(number)) / Math.log(2));
    }

    private int calculateExponent(BigDecimal number) {
        return (int) Math.floor(Math.log(Math.abs(number.doubleValue())) / Math.log(2));
    }

    private String calculateMantissa(String mantissa) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 23; i++) { // Ajuste para 23 bits de la mantisa de simple precisión
            if (i < mantissa.length()) {
                sb.append(mantissa.charAt(i));
            } else {
                sb.append("0");
            }
        }
        return sb.toString();
    }

    private String calculateMantissa(double mantissa) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 52; i++) { // Ajuste para 52 bits de la mantisa de doble precisión
            sb.append("0");
        }
        return sb.toString();
    }

    private String calculateMantissa(BigDecimal mantissa) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 112; i++) { // Ajuste para 112 bits de la mantisa de cuádruple precisión
            sb.append("0");
        }
        return sb.toString();
    }
}
