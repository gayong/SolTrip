import React, { useEffect, useState } from 'react';
import axios from 'axios';
import styles from './SelectedDate.module.css';
import { Link, useLocation } from 'react-router-dom';
import { Button } from 'antd';

function SelectedDate() {
  const [lastPlan, setLastPlan] = useState([]);
  const [planId, setPlanId] = useState('');
  const [endDate, setEndDate] = useState('');
  const [startDate, setStartDate] = useState('');
  // const [budgetData, setBudgetData] = useState([]);
  const data = localStorage.getItem('userNumber');
  const location = useLocation();

  const getDate = async () => {
    try {
      const response = await axios.get("/api2/plan", {
        headers: { "User-Number": data }
      });
      console.log('왓나?', response.data)
      const dataBody = response.data.dataBody;
      console.log('일정', lastPlan)
      if (dataBody !== null) {
        // const lastPlan = dataBody[dataBody.length - 1];
        // setLastPlan(lastPlan);
        setPlanId(dataBody.planId)
        setEndDate(dataBody.endDate)
        setStartDate(dataBody.startDate)
      }
    } catch (error) {
      console.error(error);
    }
  }

  // const getBudgetData = async (lastPlanId) => {
  //   try {
  //     const response = await axios.get(`/api2/budget/${lastPlanId}`);
  //     const dataBody = response.data.dataBody;
  //     setBudgetData(dataBody);
  //   } catch (error) {
  //     console.error(error);
  //   }
  // }

  useEffect(() => {
    getDate();
  }, []);

  // useEffect(() => {
  //   if (lastPlan) {
  //     getBudgetData(lastPlan.planId);
  //   }
  // }, [lastPlan]);

  const isButtonVisible = location.pathname !== `/plan/${lastPlan?.planId}`;

  return (
    <div>
      <h3>여행 일정</h3>
      {planId !== null ? (
        <div className={styles.dateItem}>
          <p>여행 시작 일자: {startDate}</p>
          <p>여행 종료 일자: {endDate}</p>
          <div>
            {isButtonVisible && (
              <Link to={`/plan/${planId}`}>
                <Button
                  size="small"
                  style={{
                    height: '2rem',
                    marginTop: '1rem',
                    backgroundColor: '#316FDF',
                    fontFamily: "preRg"
                  }}
                  type="primary"
                >
                  날짜별로 예산 추가하기
                </Button>
              </Link>
            )}
          </div>
        </div>
      ) : (
        <p>일정을 선택해주세요.</p>
      )}

      {/* <h3>저장된 예산</h3>
      <table>
        <thead>
          <tr>
            <th>날짜</th>
            <th>카테고리</th>
            <th>예산</th>
          </tr>
        </thead>
        <tbody>
          {budgetData.map((item) => (
            <tr key={item.budgetId}>
              <td>{item.travelDate}</td>
              <td>{item.category}</td>
              <td>{item.amount}</td>
            </tr>
          ))}
        </tbody>
      </table> */}
    </div>
  );
}

export default SelectedDate;
