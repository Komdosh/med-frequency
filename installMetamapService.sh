rm -rf /etc/systemd/system/metamap.service

echo "[Unit]
Description=MetaMap
After=syslog.target

[Service]
User=root
Type=simple
ExecStart=$METAMAP_PATH/start.sh
SuccessExitStatus=203

[Install]
WantedBy=multi-user.target" >>/etc/systemd/system/metamap.service

systemctl daemon-reload
