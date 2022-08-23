#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <stdbool.h>
#include <string.h>
#include "headers.h"

void compress(FILE* input_file, FILE* output_file, void (*encoding_function)(int, FILE*)) {
    // Inicjalizacja słownika
    Dictionary* dict = dictionary_init();
    int c = 0;
    int s;
    int index;

    if((c = getc(input_file)) == EOF) {
        printf("Wrong input file!\n");
        return;
    }

    // Wykonywanie pętli dopóki nie dojdzie do końca pliku
    while((s = getc(input_file)) != EOF) {
        // Szukanie słowa i dodanie nowego, gdy nie znajdzie
        index = dict_search_add(dict, c, (unsigned char) s);
        if(index != -1) {
            // Jest w słowniku
            c = index;
        } else {
            // Nie było w słowniku, ale już dodało
            encoding_function(c, output_file);
            c = s;
        }
    }
    encoding_function(c, output_file);
    complement(output_file);
    free_dict(dict);
}


void decompress(FILE* input_file, FILE* output_file, int (*decoding_function)(FILE*)) {
    // Inicjalizacja słownika
    Dictionary* dict = dictionary_init();

    int pk = decoding_function(input_file);
    if(pk < 0) {
        printf("Wrong input file!\n");
        return;
    }

    dict_out(dict, pk, output_file);
    int k = 0;

    // Wykonywanie pętli dopóki są jeszcze jakieś słowa kodu
    while((k = decoding_function(input_file)) != -1) {
        if(k < (dict -> size)) {
            // Słowo jest w słowniku
            add_word(dict, pk, get_first_char(dict, k));
            dict_out(dict, k, output_file);
        } else {
            // Słowa nie ma w słowniku
            add_word(dict, pk, get_first_char(dict, pk));
            dict_out(dict, dict -> size - 1, output_file);
        }
        pk = k;
    }
    free_dict(dict);
}
