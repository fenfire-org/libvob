PYTHONPATH=${0%showres11.sh}../../vob python ${0%.sh}.py | graph -TX -r .1 -u .1 -w .8 -h .8 -S 0 .015 --bitmap-size 1000x1000 "$@"
