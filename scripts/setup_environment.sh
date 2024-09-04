#!/bin/bash

# 환경 변수 파일 설정
sudo cp /home/ubuntu/app/config/wellcome_env.sh /etc/profile.d/
sudo chmod +x /etc/profile.d/wellcome_env.sh
source /etc/profile

# PEM 키 설정
sudo cp /home/ubuntu/app/config/2024wellcome.pem /home/ubuntu/
sudo chmod 400 /home/ubuntu/2024wellcome.pem

# SSH 터널링 설정 스크립트 생성
cat <<EOT | sudo tee /etc/systemd/system/ssh-tunnel.service
[Unit]
Description=SSH Tunnel for RDS
After=network.target

[Service]
ExecStart=/usr/bin/ssh -i /home/ubuntu/2024wellcome.pem -N -L 18084:db-wellcome.clwgsqaykgi6.ap-northeast-2.rds.amazonaws.com:3306 ubuntu@13.124.241.200
Restart=always
User=ubuntu

[Install]
WantedBy=multi-user.target
EOT

# systemd 서비스 리로드 및 활성화
sudo systemctl daemon-reload
sudo systemctl enable ssh-tunnel.service
sudo systemctl start ssh-tunnel.service
