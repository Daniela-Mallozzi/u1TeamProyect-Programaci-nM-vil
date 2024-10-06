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

        String fixedPoint = toFixedPointString(originalNumber, 23);
        tvFixedPoint.setText(" " + fixedPoint);

        String[] normalized = normalizeBinary(fixedPoint);
        String normalizedNumber = normalized[0].charAt(0) + "." + normalized[0].substring(1);
        tvFixedPointNormalized.setText(" " + normalizedNumber + " x 2^" + normalized[1]);

        int exponent = calculateExponent(originalNumber);
        int skewedExponent = exponent + 127; // Sesgo para precisión simple
        tvExponent.setText(" Exponent = " + exponent + "; Skewed exponent = "  + exponent + " + 127 =" + skewedExponent + " (In binary: " + String.format("%8s", Integer.toBinaryString(skewedExponent)).replace(' ', '0') + ") ");

        String mantissa = calculateMantissa(normalized[0], 23);
        tvMantissa.setText(" " + mantissa);
    }

    private void calculateDoublePrecision(double originalNumber) {
        tvOriginal.setText(" " + originalNumber);

        String fixedPoint = toFixedPointString(originalNumber, 52);
        tvFixedPoint.setText(" " + fixedPoint);

        String[] normalized = normalizeBinary(fixedPoint);
        tvFixedPointNormalized.setText(" " + normalized[0] + " x 2^" + normalized[1]);

        int exponent = calculateExponent(originalNumber);
        int skewedExponent = exponent + 1023; // Sesgo para precisión doble
        tvExponent.setText(" = " + exponent + "; Skewed exponent = " + skewedExponent + " (In binary: " + String.format("%11s", Integer.toBinaryString(skewedExponent)).replace(' ', '0') + ")");

        String mantissa = calculateMantissa(normalized[0], 52); // Asegúrate de que mantissaBits es correcto aquí
        tvMantissa.setText(" " + mantissa);
    }

    private void calculateQuadruplePrecision(BigDecimal originalNumber) {
        tvOriginal.setText(" " + originalNumber);

        String fixedPoint = toFixedPointString(originalNumber, 112); // Cuádruple precisión utiliza 112 bits para la parte fraccionaria
        tvFixedPoint.setText(" " + fixedPoint);

        String[] normalized = normalizeBinary(fixedPoint);
        tvFixedPointNormalized.setText(" " + normalized[0] + " x 2^" + normalized[1]);

        int exponent = calculateExponent(originalNumber.doubleValue());
        int skewedExponent = exponent + 16383; // Sesgo para precisión cuádruple
        tvExponent.setText(" Exponent = " + exponent + "; Skewed exponent = " + skewedExponent + " (In binary: " + String.format("%15s", Integer.toBinaryString(skewedExponent)).replace(' ', '0') + ")");

        String mantissa = calculateMantissa(normalized[0], 52);
        tvMantissa.setText(" " + mantissa);
    }

    private String toFixedPointString(float number, int fractionBits) {
        int fixedPoint = Math.round(number * (1 << fractionBits));

        String binary = Integer.toBinaryString(fixedPoint);

        int totalBits = fractionBits + 8; // 8 bits para la parte entera

        while (binary.length() < totalBits) {
            binary = "0" + binary; // Agrega ceros a la izquierda
        }

        String integerPart = binary.substring(0, binary.length() - fractionBits);
        String fractionalPart = binary.substring(binary.length() - fractionBits);

        while (integerPart.length() < 8) {
            integerPart = "0" + integerPart; // Agrega ceros a la izquierda
        }

        if (integerPart.length() > 8) {
            integerPart = integerPart.substring(integerPart.length() - 8); // Cortar a 8 bits
        }

        return String.format("%s.%s", integerPart, fractionalPart);
    }

    private String normalizeFixedPoint(float number, int fractionBits) {
        String fixedPoint = toFixedPointString(number, fractionBits);

        // Normaliza el número fijo
        String[] parts = fixedPoint.split("\\.");
        String integerPart = parts[0];
        String fractionalPart = parts[1];

        // Ajusta el número normalizado
        String normalized = integerPart + fractionalPart; // Combina parte entera y fraccionaria
        int exponent = integerPart.length() - 1; // Encuentra el exponente
        String skewedExponent = Integer.toBinaryString(exponent + 127); // Exponente sesgado

        // Normalización
        String mantissa = normalized.substring(1); // Mantissa sin el primer bit

        // Asegúrate de que la mantisa tenga 23 bits
        while (mantissa.length() < 23) {
            mantissa += "0"; // Rellena con ceros a la derecha si es necesario
        }

        return String.format("%s x2^%d", normalized.charAt(0) + "." + mantissa, exponent);
    }
    private String toFixedPointString(double number, int fractionBits) {
        long fixedPoint = Math.round(number * (1L << fractionBits)); // Multiplica por 2^fractionBits
        String binary = Long.toBinaryString(fixedPoint);

        // Calcular la longitud total que debería tener el resultado
        int totalBits = fractionBits + 1; // +1 para incluir el bit entero

        // Asegúrate de que el número binario tenga el tamaño correcto
        while (binary.length() < totalBits) {
            binary = "0" + binary; // Agrega ceros a la izquierda
        }

        String integerPart = binary.substring(0, binary.length() - fractionBits);
        String fractionalPart = binary.substring(binary.length() - fractionBits);

        // Asegurarse de que la parte entera tenga 9 bits
        while (integerPart.length() < 9) { // Asegura que tenga 9 bits (8 bits + 1 bit para el signo)
            integerPart = "0" + integerPart; // Agrega ceros a la izquierda
        }

        return String.format("%s.%s", integerPart, fractionalPart);
    }

    private String toFixedPointString(BigDecimal number, int fractionBits) {
        BigDecimal fixedPoint = number.multiply(BigDecimal.valueOf(1L << fractionBits));
        String binary = fixedPoint.toBigInteger().toString(2);

        // Calcular la longitud total que debería tener el resultado
        int totalBits = fractionBits + 1; // +1 para incluir el bit entero

        // Asegúrate de que el número binario tenga el tamaño correcto
        while (binary.length() < totalBits) {
            binary = "0" + binary; // Agrega ceros a la izquierda
        }

        String integerPart = binary.substring(0, binary.length() - fractionBits);
        String fractionalPart = binary.substring(binary.length() - fractionBits);

        while (integerPart.length() < 9) { // Asegura que tenga 9 bits (8 bits + 1 bit para el signo)
            integerPart = "0" + integerPart; // Agrega ceros a la izquierda
        }

        return String.format("%s.%s", integerPart, fractionalPart);
    }


    private String[] normalizeBinary(String binary) {
        int index = binary.indexOf('.');
        String integerPart = binary.substring(0, index);
        String fractionalPart = binary.substring(index + 1);

        // Encontrar el primer '1' en la parte entera
        int leadingOneIndex = integerPart.indexOf('1');
        if (leadingOneIndex == -1) {
            // Si no hay '1' en la parte entera, buscar en la parte fraccionaria
            leadingOneIndex = integerPart.length() + fractionalPart.indexOf('1');
            if (leadingOneIndex == -1) return new String[]{"0", "0"}; // Si no hay '1', es 0.
        }

        // Calcular el exponente real
        int exponent = (leadingOneIndex < integerPart.length()) ?
                integerPart.length() - leadingOneIndex - 1 :
                leadingOneIndex - integerPart.length();

        String normalized = "1" + integerPart.substring(leadingOneIndex + 1) + fractionalPart;
        return new String[]{normalized, String.valueOf(exponent)};
    }

    private int calculateExponent(float number) {
        if (number == 0) return 0;

        int exponent = (int) (Math.log(Math.abs(number)) / Math.log(2));
        return exponent;
    }

    private int calculateExponent(double number) {
        if (number == 0) return 0;

        int exponent = (int) (Math.log(Math.abs(number)) / Math.log(2));
        return exponent;
    }

    private String calculateMantissa(String normalized, int mantissaBits) {
        StringBuilder mantissa = new StringBuilder();
        for (int i = 1; i <= mantissaBits; i++) {
            if (i < normalized.length()) {
                mantissa.append(normalized.charAt(i));
            } else {
                mantissa.append("0");
            }
        }
        return mantissa.toString();
    }
}
