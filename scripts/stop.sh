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
  echo "$TIME_NOW > 현재 실행중인 애플리케이션이 없습니다" >> $DEPLOY_LOG
else
  echo "$TIME_NOW > 실행중인 $CURRENT_PID 애플리케이션 종료" >> $DEPLOY_LOG
  kill -15 $CURRENT_PID

  # 애플리케이션이 완전히 종료될 때까지 대기
  for i in {1..10}; do
    CURRENT_PID=$(pgrep -f $JAR_FILE)
    if [ -z "$CURRENT_PID" ]; then
      echo "$TIME_NOW > 애플리케이션이 성공적으로 종료되었습니다." >> $DEPLOY_LOG
      break
    fi
    echo "$TIME_NOW > 애플리케이션 종료 대기 중... ($i/10)" >> $DEPLOY_LOG
    sleep 3
  done

  if [ -n "$CURRENT_PID" ]; then
    echo "$TIME_NOW > 애플리케이션 종료 실패, 프로세스가 여전히 실행 중입니다." >> $DEPLOY_LOG
    exit 1
  fi
fi
