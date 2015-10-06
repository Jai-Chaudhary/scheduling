import React from 'react/addons';
import lodash from 'lodash';
import request from 'superagent-bluebird-promise';

import {avgStat, toTime, ganttHelper} from '../lib/util';
import {step} from '../lib/constant';

export default React.createClass({
  mixins: [React.addons.LinkedStateMixin],
  getInitialState() {
    let frame = this.props.frame;

    // fix the bug of patientSelect
    // default to 0 when no other
    // selection is done
    let patientSelect = null;
    frame.state.patients.forEach(
      (p, i) => {
        if (!p.arrival && patientSelect == null) {
          patientSelect = i;
        }
      }
    );

    return {
      newFrame: {
        state: JSON.parse(JSON.stringify(frame.state)),
        animation: frame.animation,
        stats: frame.stats,
      },
      siteSelect: "0",
      patientSelect: patientSelect,
      optimization: frame.state.optimization
    };
  },
  handleChange() {
    let newState = this.state.newFrame.state;
    newState.patients[parseInt(this.state.patientSelect)].site = this.state.siteSelect;
    request.post('/get_animation_stats')
    .send({state: newState, lBits: this.props.lBits}).then(
      res => {
        let data = JSON.parse(res.body.data);
        this.setState({
          newFrame: {
            animation: data.animation,
            stats: data.stats,
            state: newState
          }
        });
      }
    );
  },
  handleCommit() {
    let newState = this.state.newFrame.state;

    newState.optimization = this.state.optimization;

    request.post('/simulate_frames')
    .send({
      data: JSON.stringify({
        state: newState,
        lBits: this.props.lBits
      }),
      step: step
    })
    .then( res => {
      let newFrames = JSON.parse(res.body.data);
      this.props.updateFrames(newFrames);
    } );
  },
  render() {
    let state = this.props.frame.state;
    let stats = this.props.frame.stats;
    let newFrame = this.state.newFrame;
    let newState = newFrame.state;
    let newAnimation = newFrame.animation;
    let newStats = newFrame.stats;

    let siteOptions = Object.keys(newState.sites).map(
      i => <option value={i} key={i}>Site {i}</option>
    );

    let patientOptions = [];
    newState.patients.forEach(
      (p, i) => {
        if (!p.arrival) {
          patientOptions.push(<option value={i} key={i}>{p.name}</option>);
        }
      }
    );

    return (
      <div>
        <h1>Sandbox</h1>

        <select valueLink={this.linkState('patientSelect')}>
        {patientOptions}
        </select>

        <select valueLink={this.linkState('siteSelect')}>
        {siteOptions}
        </select>

        <button onClick={this.handleChange}>Change</button>

        <label>Optimizer</label>
        <input type="checkbox" checkedLink={this.linkState('optimization')} />

        <button onClick={this.handleCommit}>Commit</button>
        <button onClick={this.props.handleToAnimation}>Cancel</button>
        <div>
          Avg waiting time: {avgStat(newStats)}
          &nbsp; Change of avg waiting time: {lodash.round(avgStat(newStats) - avgStat(stats), 2)}
        </div>

        {ganttHelper(newState, newAnimation, newStats)}
      </div>
    );
  }
});
