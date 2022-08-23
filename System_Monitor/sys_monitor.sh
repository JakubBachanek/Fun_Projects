#!/bin/bash

numOfInt=$(( $( cat /proc/net/dev | wc -l ) - 2 ))

currentDownload=0
currentUpload=0

avgDownload=0
avgUpload=0

counterD=0
counterU=0

uptime=0
dni=0
godziny=0
minuty=0
sekundy=0

beteria=0

loadAvg1=0
loadAvg5=0
loadAvg15=0

downArray=()
uplArray=()
downMax=0
uplMax=0

#### USTAWIENIA WYSOKOSCI WYKRESU

wysokoscW=13
dlugoscW=30
skalaD=0
skalaU=0
downWysokoscArray=()
uplWysokoscArray=()

for (( i = 0; i < 10; i++))
do
    downArray+=(0)
    uplArray+=(0)
    downWysokoscArray+=(0)
    uplWysokoscArray+=(0)
done

kolorD=$(tput setaf 2)
kolorU=$(tput setaf 4)
kolorZ=$(tput sgr0)
trap "tput clear; exit" SIGINT SIGTERM
tput civis

function obliczPredkosc() {

    tempD1=0
    tempU1=0

    for (( i = 0; i < $numOfInt; i++ ))
    do
        index=$(( 3 + i ))
        tempCurD=$( awk -v j=$index 'NR == j {print $2}' /proc/net/dev )
        tempCurU=$( awk -v j=$index 'NR == j {print $10}' /proc/net/dev )
        tempD1=$(( $tempD1 + $tempCurD ))
        tempU1=$(( $tempU1 + $tempCurU ))
    done

    sleep 1

    tempD2=0
    tempU2=0

    for ((i = 0; i < $numOfInt; i++))
    do
        index=$(( 3 + i ))
        tempCurD=$( awk -v j=$index 'NR == j {print $2}' /proc/net/dev )
        tempCurU=$( awk -v j=$index 'NR == j {print $10}' /proc/net/dev )
        tempD2=$(( $tempD2 + $tempCurD ))
        tempU2=$(( $tempU2 + $tempCurU ))
    done

    currentDownload=$(( $tempD2 - $tempD1 ))
    currentUpload=$(( $tempU2 - $tempU1 ))
}

function obliczAvgPredkosc() {

    (( counterD++ ))
    (( counterU++ ))
    avgDownload=$(( $avgDownload + ($currentDownload - $avgDownload) / counterD))
    avgUpload=$(( $avgUpload + ($currentUpload - $avgUpload) / counterU))
}


function obliczJednostki() {

    if [ $1 -lt 1000 ]
    then
        echo " $1 B/s"
    elif [ $1 -lt 1000000 ]
    then
        temptemp1=$( echo "scale=2; $1 / 1000" | bc -l )
        echo " $temptemp1 KB/s"
    else
        temptemp2=$( echo "scale=2; $1 / 1000000" | bc -l )
        echo " $temptemp2 MB/s"
    fi
}


function obliczCzas() {

    uptime=$( echo $( awk '{print $1}' /proc/uptime ) / 1 | bc )
    dni=$( echo $uptime / 3600 / 24 | bc )
    godziny=$( echo $uptime / 3600 % 24 | bc )
    minuty=$( echo $uptime / 60 % 60 | bc )
    sekundy=$( echo $uptime % 60 | bc)
}


function obliczBaterie() {

    bateria=$( awk '/POWER_SUPPLY_CAPACITY=/' /sys/class/power_supply/BAT0/uevent | grep -o '[0-9]\+' )
}

function obliczLoadAvg() {

    loadAvg1=$( awk '{print $1}' /proc/loadavg )
    loadAvg5=$( awk '{print $2}' /proc/loadavg )
    loadAvg15=$( awk '{print $3}' /proc/loadavg )
}
function wypiszDane() {

    echo -n -e "Current speed: \n"
    printf "D:%2s     \n" "$(obliczJednostki $currentDownload)"
    printf "U:%2s     \n" "$(obliczJednostki $currentUpload)"
    echo -n -e "\nAverage speed: \n"
    printf "D:%2s     \n" "$(obliczJednostki $avgDownload)"
    printf "U:%2s     \n" "$(obliczJednostki $avgUpload)"
    echo -n -e "\nUptime: \n"
    echo -n -e "  $dni d $godziny h $minuty min $sekundy sec      \n\n"
    echo -n -e "Bateria: \n"
    printf "  %2s %s    \n" "$bateria" "%"
    echo -n -e "\nAverage load: \n"
    echo -e "  Last 1 min: $loadAvg1 \n  Last 5 mins: $loadAvg5 \n  Last 15 mins $loadAvg15    "
    echo -e "\n"
}


function arrayPrzesunDodaj() {
	for (( i = 0; i < 9; i++ ))
    do
        downArray[$i]=${downArray[(($i+1))]}
        uplArray[$i]=${uplArray[(($i+1))]}
    done

    downArray[-1]=$1
    uplArray[-1]=$2
}


function obliczMaxy() {
    downMax=${downArray[0]}
    uplMax=${uplArray[0]}
    arrayDl=${#downArray[@]}
    for (( i = 1; i < $arrayDl; i++ ))
    do
        if (( ${downArray[$i]} > $downMax ))
        then
            downMax=${downArray[$i]}
        fi
        if (( ${uplArray[$i]} > $uplMax ))
        then
            uplMax=${uplArray[$i]}
        fi
    done
}


function obliczSkale() {

    skalaD=$(( downMax / wysokoscW ))
    skalaU=$(( uplMax / wysokoscW ))
}

function obliczWysokoscSlupkow() {

    arrayDl=${#downArray[@]}

    for (( i = 0; i < $arrayDl; i++ ))
    do
        if (( $skalaD == 0 ))
        then
            downWysokoscArray[$i]=0
        else
            downWysokoscArray[$i]=$(( ${downArray[$i]} / $skalaD ))
        fi

        if (( $skalaU == 0 ))
        then
            uplWysokoscArray[$i]=0
        else
            uplWysokoscArray[$i]=$(( ${uplArray[$i]} / $skalaU ))
        fi
    done
}

function rysujWykres() {
    local -n arrayTest=$1
    skalaTest=$2
    kolor=$3

    printf "%14s"
    for (( i = 0; i <= 2 * $dlugoscW + 1; i++ ))
    do
        printf "-"
    done
    printf "\n"
    for (( i = 1; i <= $wysokoscW; i++ ))
    do
        if (( i % 2 == 1 ))
        then
            printf "%13s %s" "$(obliczJednostki $(( $skalaTest * ($wysokoscW - $i + 1) )))" "|"
        else
            printf "%15s" "|"
        fi
        linijka=""
        for (( j = 0; j < $dlugoscW; j = j + 3))
        do

            if (( $(( $wysokoscW - $i )) < ${arrayTest[$(( $j / 3))]} ))
            then
                for (( k=0; k<2; k++))
                do

                    linijka+="$kolor\u233B$kolorZ"

                done
                linijka="$linijka\u0020\u0020\u0020\u0020"
            else
                linijka="$linijka\u0020\u0020\u0020\u0020\u0020\u0020"
            fi

        done

        echo -n -e $linijka

    printf "|\n"
	done

    printf "%14s"
    for (( i = 0; i <= 2 * $dlugoscW + 1; i++ ))
    do
        printf "-"
    done
}


while(true)
do
    tput cup 1 0
    obliczPredkosc
    obliczAvgPredkosc
    obliczCzas
    obliczBaterie
    obliczLoadAvg

    arrayPrzesunDodaj $currentDownload $currentUpload
    obliczMaxy
    tput clear
    wypiszDane

    obliczSkale
    obliczWysokoscSlupkow
    rysujWykres downWysokoscArray $skalaD $kolorD
    echo -e "\n"
    rysujWykres uplWysokoscArray $skalaU $kolorU
done
