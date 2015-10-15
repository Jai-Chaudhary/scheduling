import React from 'react/addons';
import request from 'superagent-bluebird-promise';

import {step} from '../lib/constant';

let config = null;
let replayConfig = null;
let historyDate = '2013-04-15';

export default React.createClass({
  mixins: [React.addons.LinkedStateMixin],
  getInitialState() {
    let state = { config: 'Loading', replayConfig: 'Loading', historyDate: historyDate};
    if (config != null) {
      state.config = config;
    }
    if (replayConfig != null) {
      state.replayConfig = replayConfig;
    }
    return state;
  },
  componentDidMount() {
    if (config == null) {
      request.get('/static/example_config.json').then(
        data => this.setState({ config: data.text })
      );
    }

    if (replayConfig == null) {
      request.get('/static/replay_config.json').then(
        data => this.setState({ replayConfig: data.text})
      );
    }
  },
  handleGo() {
    let lBits;
    request.post('/configparser')
      .send({data: this.state.config}).then(
        res => {
          lBits = JSON.parse(res.body.data).lBits;
          return request.post('/simulate_frames')
          .send({data: res.body.data, step:step}).promise();
        }
      ).then(
        res => {
          return this.props.handleToPlayroom(JSON.parse(res.body.data), lBits);
        }
      );
  },
  handleReplayGo() {
    let lBits;
    request.post('/get_history_blob')
      .send(JSON.parse(this.state.replayConfig)).then(
        res => {
          lBits = JSON.parse(res.body.data).lBits;
          return request.post('/simulate_frames')
          .send({data: res.body.data, step:step}).promise();
        }
      ).then(
        res => {
          return this.props.handleToPlayroom(JSON.parse(res.body.data), lBits);
        }
      );
  },
  handleHistoryGo() {
    request.post('/get_history_schedule').send({day: this.state.historyDate}).then(
      res => this.props.handleToHistory(res.body.schedule)
    );
  },
  componentWillUnmount() {
    config = this.state.config;
    replayConfig = this.state.replayConfig;
    historyDate = this.state.historyDate;
  },
  render() {
    return (
      <div>
        <h1>Config</h1>

        <div>
          <button onClick={this.handleGo}>Go</button>
          <br />
          <textarea valueLink={this.linkState('config')} style={{width:'600px',height:'500px'}}></textarea>
        </div>

        <div>
          <button onClick={this.handleReplayGo}>Go</button>
          <br />
          <textarea valueLink={this.linkState('replayConfig')} style={{width:'600px',height:'500px'}}></textarea>
        </div>

        <div>
          <input type="text" valueLink={this.linkState('historyDate')} />
          <button onClick={this.handleHistoryGo}>Go</button>
        </div>
      </div>
    );
  }
});
