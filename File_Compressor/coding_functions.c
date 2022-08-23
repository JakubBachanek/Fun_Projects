#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <stdbool.h>
#include <string.h>
#include "headers.h"
extern bool fibo_flag;

// Kodowanie Eliasa gamma

void gamma_encode(int c, FILE* output_file) {
    int buffer[128] = {0};
    int n = 0;
    c++;

    // Zamiana liczby c na zapis dwójkowy
    while(c > 0) {
        buffer[n] = c & 1;
        c >>= 1;
        n++;
    }

    // Dodanie bitu o wartości zero powtórzonego (n - 1) razy
    for(int i = 0; i < n - 1; i++) {
        add_bit(output_file, 0);
    }

    // Dodanie liczby c zapisanej dwójkowo
    for(int i = n - 1; i >= 0; i--) {
        add_bit(output_file, (unsigned char) buffer[i]);
    }
}

int gamma_decode(FILE* input_file) {
    int n = 1;
    int bit;

    // Zliczanie bitów o wartości zero
    while((bit = read_bit(input_file)) == 0) {
        n++;
    }
    if(bit == -1) {
        return -1;
    }

    // Odczytywanie liczby zapisanej binarnie w kolejnych (n + 1) bitach
    int temp = 1;
    for(int i = 1; i < n; i++) {
        temp <<= 1;
    }
    int number = temp;
    temp >>= 1;

    for(int i = 0; i < n - 1; i++) {
        if((bit = read_bit(input_file)) != -1) {
            number += bit * temp;
            temp >>= 1;
        } else {
            return -1;
        }
    }
    number--;
    return number;
}


// Kodowanie Eliasa delta

void delta_encode(int c, FILE* output_file) {
    int buffer_1[64] = {0};
    int buffer_2[128] = {0};
    int n = 0;
    int k = 0;
    c++;

    // Zamiana liczby c na zapis dwójkowy
    while(c > 0) {
        buffer_1[n] = c & 1;
        c >>= 1;
        n++;
    }

    // Zamiana liczby temp na zapis dwójkowy
    int temp = n;
    while(temp > 0) {
        buffer_2[k] = temp & 1;
        temp >>= 1;
        k++;
    }

    // Dodanie bitu o wartości zero powtórzonego (k - 1) razy
    // Dodanie liczby temp, a następnie c (bez najbardziej znaczącej cyfry), zapisanych dwójkowo
    for(int i = 0; i < k - 1; i++) {
        add_bit(output_file, 0);
    }
    for(int i = k - 1; i >= 0; i--) {
        add_bit(output_file, (unsigned char) buffer_2[i]);
    }
    for(int i = n - 2; i >= 0; i--) {
        add_bit(output_file, (unsigned char) buffer_1[i]);
    }
}


int delta_decode(FILE* input_file) {
    int k = 1;
    int bit;

    // Wczytywanie zer dopóki nie natrafi na jedynkę
    while((bit = read_bit(input_file)) == 0) {
        k++;
    }
    if(bit == -1) {
        return -1;
    }

    // Odczytywanie liczby zakodowanej binarnie w kilku etapach
    int temp = 1;
    for(int i = 0; i < k - 1; i++) {
        temp <<= 1;
    }
    int n = temp;
    temp >>= 1;

    for(int i = 0; i < k - 1; i++) {
        if((bit = read_bit(input_file)) != -1) {
            n += bit * temp;
            temp >>= 1;
        } else {
            return -1;
        }
    }
    temp = 1;
    for(int i = 0; i < n - 1; i++) {
        temp <<= 1;
    }

    int number = temp;
    temp >>= 1;

    for(int i = 0; i < n - 1; i++) {
        if((bit = read_bit(input_file)) != -1) {
            number += bit * temp;
            temp >>= 1;
        } else {
            return -1;
        }
    }
    number--;
    return number;
}


// Kodowanie Eliasa omega

void omega_encode(int c, FILE* output_file) {
    int buffer_1[64] = {0};
    int buffer_2[128] = {0};
    int n = 1;
    c += 2;
    int k = 0;
    // Wykonywanie pętli dopóki c nie osiągnie 1
    while(c > 1) {
        int temp = c;
        k = 0;

        // Zapisywanie dwójkowo liczby temp (czyli c)
        while(temp > 0) {
            buffer_1[k] = temp & 1;
            temp >>= 1;
            k++;
        }
        // Dopisywanie bitów do bufora_2 oraz ustalanie nowego c
        for(int i = 0; i < k; i++) {
            buffer_2[n] = buffer_1[i];
            n++;
        }
        c = k - 1;
    }

    // Wypisywanie bitów do pliku
    for(int i = n - 1; i >= 0; i--) {
        add_bit(output_file, (unsigned char) buffer_2[i]);
    }
}


int omega_decode(FILE* input_file) {
    int n = 1;
    int bit = 0;
    bool flag = true;

    // Wykonywanie pętli dopóki nie natrafi na 0
    while((bit = read_bit(input_file)) == 1) {
        int temp = 1;
        flag = false;

        // Dodawanie do n wartości zapisanej na (n + 1) kolejnych bitach
        for(int i = 0; i < n; i++) {
            temp <<= 1;
        }
        int value = temp;
        temp >>= 1;

        for(int i = 0; i < n; i++) {
            if((bit = read_bit(input_file)) != -1) {
                value += bit * temp;
                temp >>= 1;
            } else {
                return -1;
            }
        }
        n = value;
    }
    if(bit == -1 || flag) {
        return -1;
    }
    n -= 2;
    return n;
}


// Kodowanie Fibonacciego

void fibo_encode(int c, FILE* output_file) {
    int buffer[128] = {0};
    static int fibo[30] = {0};
    int n = 0;
    c++;

    // Inicjalizacja tablicy liczb Fibonacciego
    if(fibo_flag) {
        fibo[0] = 1;
        fibo[1] = 2;
        for(int i = 2; i < 30; i++) {
            fibo[i] = fibo[i - 1] + fibo[i - 2];
        }
        fibo_flag = false;
    }

    // Szukanie największej z liczb Fib. mniejszej lub równej od c
    int max = 0;
    while(fibo[max + 1] <= c) {
        max++;
    }

    // Dodawanie bitów 1 lub 0 do bufora jako współczynników
    c -= fibo[max];
    buffer[n] = 1;
    max--;
    n++;

    while(c > 0) {
        if(fibo[max] <= c) {
            c -= fibo[max];
            buffer[n] = 1;
            n++;
        } else {
            buffer[n] = 0;
            n++;
        }
        max--;
    }
    while(max >= 0) {
        buffer[n] = 0;
        max--;
        n++;
    }

    // Wypisanie bitów do pliku
    for(int i = n - 1; i >= 0; i--) {
        add_bit(output_file, (unsigned char) buffer[i]);
    }

    // Dodanie ostatniego bitu (zawsze równego 1)
    add_bit(output_file, (unsigned char) 1);
}


int fibo_decode(FILE* input_file) {
    // Inicjalizacja tablicy liczb Fibonacciego
    static int fibo[30] = {0};
    if(fibo_flag) {
        fibo[0] = 1;
        fibo[1] = 2;
        for(int i = 2; i < 30; i++) {
            fibo[i] = fibo[i - 1] + fibo[i - 2];
        }
        fibo_flag = false;
    }

    // Wczytanie pierwszego bitu
    int previous = read_bit(input_file);
    if(previous == -1) {
        return -1;
    }
    int bit;
    int n = 0;
    int number = previous * fibo[n];
    n++;

    // Wczytywanie kolejnych bitów jako współczynników i uzupełnianie wyniku
    while((bit = read_bit(input_file)) != -1) {
        if(bit == 1 && previous == 1) {
            number--;
            return number;
        } else {
            number += bit * fibo[n];
            previous = bit;
            n++;
        }
    }
    return -1;
}
