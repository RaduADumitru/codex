exec echo "start script"
yum install -y nc
for count in {1..100}; do
	echo "Pinging ArangoDB database attempt "${count}
        if  $(nc -z 127.0.0.1 8529) ; then
        	echo "Can connect into database"
        	break
        fi
        sleep 5
    done

exec java -jar /usr/local/lib/codex.jar
