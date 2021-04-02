rm -rf /etc/systemd/system/med-frequency.service

echo "[Unit]
Description=Med Frequency
After=syslog.target

[Service]
User=root
ExecStart=/usr/bin/java -Xms512m -Xmx2048m -jar /opt/med-frequency-0.0.1.jar
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target" >>/etc/systemd/system/med-frequency.service

systemctl daemon-reload
