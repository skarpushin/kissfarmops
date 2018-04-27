echo Checking status of the operation

value=`cat $INSTANCE_FOLDER/custom_file.txt`
expected=armed_flag

if [ `$value` == `$expected` ]
then
    exit 0 # ok
else
    exit 1 # failure
fi
