#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <stdbool.h>
#include <string.h>
#include "headers.h"

// Inicjalizacja słownika
Dictionary* dictionary_init() {
    Dictionary* dict = malloc(sizeof(Dictionary));
    dict -> words = malloc(sizeof(Word*) * DICT_MAX_SIZE);
    dict -> size = 0;
    for(int i = 0; i < 256; i++) {
        Word* word = malloc(sizeof(Word));
        word -> prefix = -1;
        word -> c = i;
        word -> first = -2;
        word -> left = -2;
        word -> right = -2;
        dict -> words[dict -> size] = word;
        (dict -> size)++;
    }
    return dict;
}


// Wyszukiwanie słowa, jeśli nie znalazło to dodanie nowego
int dict_search_add(Dictionary* dict, int prefix, unsigned char c) {
    int index = dict -> words[prefix] -> first;
    if(index == -2) {
        dict -> words[prefix] -> first = dict -> size;
    } else {
        while(true) {
            if(c == dict -> words[index] -> c) {
                return index;
            } else if(c < (dict -> words[index] -> c)) {
                int next = dict -> words[index] -> left;
                if(next == -2) {
                    dict -> words[index] -> left = dict -> size;
                    break;
                }
                index = next;
            } else {
                int next = dict -> words[index] -> right;
                if(next == -2) {
                    dict -> words[index] -> right = dict -> size;
                    break;
                }
                index = next;
            }
        }
    }
    if(dict -> size == DICT_MAX_SIZE) {
        dict_reset(dict);
    } else {
        Word* word = malloc(sizeof(Word));
        word -> prefix = prefix;
        word -> c = c;
        word -> first = -2;
        word -> left = -2;
        word -> right = -2;
        dict -> words[dict -> size] = word;
        (dict -> size)++;
    }
    return -1;
}

// Dodawanie nowego słowa do słownika (dla dekompresji)
void add_word(Dictionary* dict, int index, unsigned char c) {
    if(dict -> size == DICT_MAX_SIZE) {
        dict_reset(dict);
        return;
    }
    Word* word = malloc(sizeof(Word));
    word -> prefix = index;
    word -> c = c;
    dict -> words[dict -> size] = word;
    (dict -> size)++;
}

// Wypisywanie słowa
void dict_out(Dictionary* dict, int i, FILE* output_file) {
    if(dict -> words[i] -> prefix >= 0) {
        dict_out(dict, dict -> words[i] -> prefix, output_file);
    }
    fputc(dict -> words[i] -> c, output_file);
}

// Zwrócenie pierwszego znaku słowa
unsigned char get_first_char(Dictionary* dict, int i) {
    while(dict -> words[i] -> prefix != -1) {
        i = dict -> words[i] -> prefix;
    }
    return dict -> words[i] -> c;
}

// Resetowanie słownika
void dict_reset(Dictionary* dict) {
    for(int i = 256; i < (dict -> size); i++) {
        free(dict -> words[i]);
    }
    for(int i = 0; i < 256; i++) {
        dict -> words[i] -> first = -2;
        dict -> words[i] -> left = -2;
        dict -> words[i] -> right = -2;
    }
    dict -> size = 256;
}

// Zwalnienie pamięci zajmowanej przez słownik
void free_dict(Dictionary* dict) {
    for(int i = 0; i < (dict -> size); i++) {
        free(dict -> words[i]);
    }
    free(dict -> words);
    free(dict);
}
