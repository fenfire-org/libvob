python ${0%.sh}.py | graph -TX -r .1 -u .1 -w .8 -h .8 -S 0 .015 --bitmap-size 1000x1000 "$@"
#awk '{print "#m=" 1+$14 ",S=" 16-12*$14 "\n" $2 " " $5 " " $8 " " $11}'|graph -TX -r .1 -u .1 -w .8 -h .8 -S 0 .015 --bitmap-size 1000x1000 "$@"
