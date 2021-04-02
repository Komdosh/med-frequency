rm -rf /etc/systemd/system/metamap.service

echo "[Unit]
Description=MetaMap
After=syslog.target

[Service]
User=root
Type=simple
ExecStart=$MED_FREQUENCY_WORKDIR/public_mm/start.sh
SuccessExitStatus=203

[Install]
WantedBy=multi-user.target" >>/etc/systemd/system/metamap.service

systemctl daemon-reload
