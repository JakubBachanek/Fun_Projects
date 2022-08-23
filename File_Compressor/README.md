# File Compressor

## Opis projektu

Program do kompresji i dekompresji danych z wykorzystaniem metody **bezstratnej kompresji słownikowej LZW**. Użyte zostało **kodowanie Eliasa: gamma, delta, omega**, oraz **kodowanie Fibonacciego**.

## Kompilacja

make all

## Uruchomienie

KOMPRESJA: ./lzw -c -t [KODOWANIE] -i [INPUT] -o [OUTPUT]  

DEKOMPRESJA: ./lzw -d -t [KODOWANIE] -i [INPUT] -o [OUTPUT]  

flaga -t akceptuje: gamma, delta, omega, fib  
