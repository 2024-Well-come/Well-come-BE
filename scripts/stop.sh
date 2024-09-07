#!/bin/bash

PROJECT_ROOT="/home/ubuntu/spring-github-action"
JAR_FILE="$PROJECT_ROOT/spring-webapp.jar"

DEPLOY_LOG="$PROJECT_ROOT/deploy.log"

TIME_NOW=$(date +%c)

# 로그 파일 생성 및 소유자 변경
touch $DEPLOY_LOG
chown ubuntu:ubuntu $DEPLOY_LOG

# 현재 구동 중인 애플리케이션 pid 확인
CURRENT_PID=$(pgrep -f $JAR_FILE)

# 프로세스가 켜져 있으면 종료
if [ -z "$CURRENT_PID" ]; then
  echo "$TIME_NOW > 현재 실행 중인 애플리케이션이 없습니다." >> $DEPLOY_LOG
else
  echo "$TIME_NOW > 실행 중인 애플리케이션 (PID: $CURRENT_PID) 종료 시도" >> $DEPLOY_LOG
  kill -15 $CURRENT_PID

  # 프로세스가 종료되지 않은 경우 강제 종료
  sleep 5
  if pgrep -f $JAR_FILE > /dev/null; then
    echo "$TIME_NOW > 프로세스가 여전히 실행 중입니다. 강제 종료합니다." >> $DEPLOY_LOG
    kill -9 $CURRENT_PID
  fi
fi
