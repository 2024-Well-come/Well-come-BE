##!/bin/bash
#
## 환경 변수 파일 설정
#source ~/.bash_profile
#
## PEM 키 설정
#sudo chmod 400 ${SSH_PRIVATE_KEY_PATH}
#
## SSH 터널링 설정 스크립트 생성
#cat <<EOT | sudo tee /etc/systemd/system/ssh-tunnel.service
#[Unit]
#Description=SSH Tunnel for RDS
#After=network.target
#
#[Service]
#ExecStart=/usr/bin/ssh -i ${SSH_PRIVATE_KEY_PATH} -N -L ${SSH_LOCAL_PORT}:${DB_ENDPOINT}:${DB_PORT} ${SSH_USER}@${SSH_REMOTE_JUMP_HOST}
#Restart=always
#User=ubuntu
#
#[Install]
#WantedBy=multi-user.target
#EOT
#
## systemd 서비스 리로드 및 활성화
#sudo systemctl daemon-reload
#sudo systemctl enable ssh-tunnel.service
#sudo systemctl start ssh-tunnel.service
