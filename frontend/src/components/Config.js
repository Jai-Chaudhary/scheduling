import React from 'react/addons';
import request from 'superagent-bluebird-promise';

import {step} from '../lib/constant';

let config = null;
let replayConfig = null;

export default React.createClass({
  mixins: [React.addons.LinkedStateMixin],
  getInitialState() {
    let state = { config: 'Loading', replayConfig: 'Loading'};
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
          console.log(JSON.parse(res.body.data));
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
  componentWillUnmount() {
    config = this.state.config;
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
      </div>
    );
  }
});
