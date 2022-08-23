#define DICT_MAX_SIZE 4096 * 16

typedef struct Word {
    int prefix;
    int first;
    int left;
    int right;
    unsigned char c;
} Word;

typedef struct Dictionary {
    Word** words;
    int size;
} Dictionary;


void compress(FILE* input_file, FILE* output_file, void (*encoding_function)(int, FILE*));
void decompress(FILE* input_file, FILE* output_file, int (*decoding_function)(FILE*));

void gamma_encode(int c, FILE* output_file);
int gamma_decode(FILE* input_file);

void delta_encode(int c, FILE* output_file);
int delta_decode(FILE* input_file);

void omega_encode(int c, FILE* output_file);
int omega_decode(FILE* input_file);

void fibo_encode(int c, FILE* output_file);
int fibo_decode(FILE* input_file);

int add_bit(FILE* output_file, unsigned char bit);
int read_bit(FILE* input_file);
void complement(FILE* output_file);
void calculate_entropy(double* entropy1, double* entropy2, long int* input_file_size, long int* output_file_size, double* compression_rate, char* input_file_name, char* output_file_name);

Dictionary* dictionary_init();
void add_word(Dictionary* dict, int index, unsigned char ch);
void dict_out(Dictionary* dict, int index, FILE* output_file);
void free_dict(Dictionary* dict);
void dict_reset(Dictionary* dict);
unsigned char get_first_char(Dictionary* dict, int index);
int dict_search_add(Dictionary* dict, int prefix, unsigned char c);
