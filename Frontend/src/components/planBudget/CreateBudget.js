import React, { useState } from 'react';
import { Select, InputNumber, Button, Space } from 'antd';
import axios from "axios";
import styles from './CreateBudget.module.css';
import DeleteBuget from './DeleteBudget';

const { Option } = Select;

const CreateBudget = () => {
  const [category, setCategory] = useState('');
  const [amount, setAmount] = useState('');

  const handleCategoryChange = (value) => {
    setCategory(value);
  };

  const handleAmountChange = (value) => {
    setAmount(value);
  };

  // 백엔드랑 연결후에 수정하기!!!
  const handleSubmit = async () => {
    try {
      const requestData = {
        dataBody: { 
          "travelDate":"2023-08-25", // 날짜 변경 요망
          "category" : "교통비",
          "amount" : "30000"
        },
      };
    const response = await axios.post("/budget/{plan_id}", requestData);
    console.log(response.data)
      if (response.ok) {
        console.log('성공');
      } else {
        console.error('추가 중 오류');
      }
    } catch (error) {
      console.error('그냥 에러', error);
    }
  };

  return (
    <div>
      <h3>예산 추가하기</h3>
      <form className={styles.dateItem}>
        <label className={styles.category}>
          <p>카테고리:</p>
          <Select
            placeholder="선택"
            optionFilterProp="children"
            onChange={handleCategoryChange}
            style={{ width: '30%' }}
          >
            <Option value="food">식비</Option>
            <Option value="transportation ">교통비</Option>
            <Option value="shopping">쇼핑</Option>
          </Select>
        </label>
        <br />
        <br />
          <label className={styles.amount}>
          <p>예산:</p>
          <Space.Compact>
          <InputNumber addonAfter="₩"
            min={1}
            value={amount}
            onChange={handleAmountChange}
            style={{ width: '80%' }} 
            formatter={(value) => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
            parser={(value) => value.replace(/\₩\s?|(,*)/g, '')}
          />
          </Space.Compact>
        </label>
        <br />
        <br />
        <Button type="primary" onClick={handleSubmit}>
          예산 추가하기
        </Button>
        <DeleteBuget />
      </form>
    </div>
  );
};

export default CreateBudget;
