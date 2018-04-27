echo Writing result to $RESULT_FILE
echo {\"result\": \"$VAR1\"} >$RESULT_FILE

echo Writing custom data to custom file $INSTANCE_FOLDER/custom_file.txt
echo 'armed_flag' >$INSTANCE_FOLDER/custom_file.txt

echo Exiting as async opertation
exit 3

