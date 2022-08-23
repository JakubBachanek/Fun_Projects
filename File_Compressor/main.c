#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <stdbool.h>
#include <string.h>
#include <math.h>
#include "headers.h"

bool fibo_flag = false;

int main(int argc, char** argv) {

    // Inicjalizacja potrzebych zmiennych
    bool compress_flag = false;
    bool decompress_flag = false;
    double entropy1 = 0;
    double entropy2 = 0;
    void (*encoding_functions[])(int, FILE*) = { omega_encode, gamma_encode, delta_encode, fibo_encode };
    int (*decoding_functions[])(FILE*) = { omega_decode, gamma_decode, delta_decode, fibo_decode };
    int coding_index = 0;
    char* input_file_name;
    char* output_file_name;
    int opt = 0;

    // Sprawdzanie argumentów programu

    while((opt = getopt(argc, argv, "cdt:i:o:")) != -1) {
        switch(opt) {
            case 'c':
                compress_flag = true;
                break;
            case 'd':
                decompress_flag = true;
                break;
            case 't':
                if(!strcmp(optarg, "omega")) {
                    coding_index = 0;
                } else if(!strcmp(optarg, "gamma")) {
                    coding_index = 1;
                } else if(!strcmp(optarg, "delta")) {
                    coding_index = 2;
                } else if(!strcmp(optarg, "fib")) {
                    fibo_flag = true;
                    coding_index = 3;
                } else {
                    printf("Wrong -t flag argument\n");
                    return -1;
                }
                break;
            case 'i':
                input_file_name = optarg;
                break;
            case 'o':
                output_file_name = optarg;
                break;
        }
    }

    // Otworzenie plików input i output

    FILE* input_file = fopen(input_file_name, "rb");
    FILE* output_file = fopen(output_file_name, "wb");

    if(input_file == NULL || output_file == NULL) {
        printf("Error with files\n");
        return -2;
    }

    // Wywołanie funkcji kompresji / dekompresji

    if(compress_flag) {
        compress(input_file, output_file, encoding_functions[coding_index]);
    } else if(decompress_flag) {
        decompress(input_file, output_file, decoding_functions[coding_index]);
    } else {
        printf("Wrong arguments!\n");
        return -3;
    }
    fclose(input_file);
    fclose(output_file);
    long int input_file_size = 0;
    long int output_file_size = 0;
    double compression_rate = 0;
    calculate_entropy(&entropy1, &entropy2, &input_file_size, &output_file_size, &compression_rate, input_file_name, output_file_name);

    printf("Długość input = %ld\n", input_file_size);
    printf("Długość output = %ld\n", output_file_size);
    printf("Entropia input = %f\n", entropy1);
    printf("Entropia output = %f\n", entropy2);
    printf("Stopień kompresji = %f\n", compression_rate);
    return 0;
}

// Funkcja dodająca jeden bit do bufora, który następnie jest zapisywany jako bajt do pliku

int add_bit(FILE* output_file, unsigned char bit) {
    static unsigned char byte = 0;
    static int length = 0;
    byte <<= 1;
    byte |= bit;
    length++;
    if(length == 8) {
        fputc(byte, output_file);
        length = 0;
        return 0;
    }
    return length;
}

// Funkcja czytająca jeden bajt z pliku, i zwracająca pojedyncze bity z tego bajta

int read_bit(FILE* input_file) {
    static unsigned char byte = 0;
    static int length = 0;
    int i;
    int bit;
    if(length == 0) {
        if((i = getc(input_file)) != EOF) {
            byte = (unsigned char) i;
        } else {
            return -1;
        }
        length = 8;
    }
    bit = byte >> 7;
    byte <<= 1;
    length -= 1;

    return bit;
}

// Funckja dopełniająca bajta zerami

void complement(FILE* output_file) {
    while(add_bit(output_file, 0) > 1);
}

// Funkcja do obliczania entropii i rozmiarów

void calculate_entropy(double* entropy1, double* entropy2, long int* input_file_size, long int* output_file_size, double* compression_rate, char* input_file_name, char* output_file_name) {
    unsigned long int count_all_1 = 0;
    unsigned long int count_all_2 = 0;
    long long int count_1[256] = {0};
    long long int count_2[256] = {0};
    int ch_1;
    int ch_2;
    FILE* input_file = fopen(input_file_name, "rb");
    FILE* output_file = fopen(output_file_name, "rb");
    while((ch_1 = getc(input_file)) != EOF) {
        count_1[ch_1]++;
        count_all_1++;
    }
    while((ch_2 = getc(output_file)) != EOF) {
        count_2[ch_2]++;
        count_all_2++;
    }
    for(int i = 0; i < 256; i++) {
        if(count_1[i] > 0) {
            double probability = (double) count_1[i] / count_all_1;
            (*entropy1) += (-1.0) * probability * log2(probability);
        }
        if(count_2[i] > 0) {
            double probability = (double) count_2[i] / count_all_2;
            (*entropy2) += (-1.0) * probability * log2(probability);
        }
    }
    (*input_file_size) = 8 * count_all_1;
    (*output_file_size) = 8 * count_all_2;
    (*compression_rate) = (double) count_all_1 / count_all_2;
}
