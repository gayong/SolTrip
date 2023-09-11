import React from 'react';
import { Link } from 'react-router-dom'
import styles from '../pages/PlanBudget.module.css'
import Header from "../components/common/Header";
import BudgetList from "../components/planBudget/BudgetList"

import { Button } from 'antd';
import styles from './PlanBudget.module.css'

const Planbudget = () => {
  // const reloadPage = () => {
  //   window.location.reload();
  // }

  return (
    <div>
     <div>
      <Header/>
      {/* <Button onClick={reloadPage}><Link to="/plan">뒤로가기</Link></Button> */}
      <div onClick={reloadPage} className={styles.titles}>
        <Link className={styles.backLink} to='/plan'><p className={styles.toBack}>&lt;</p>
        <p className={styles.popularTitle}>뒤로가기</p></Link> 
      </div>
      <BudgetList/>
      </div>
    </div>
  );
};

export default Planbudget;
