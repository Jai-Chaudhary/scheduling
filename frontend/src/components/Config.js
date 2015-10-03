import React from 'react/addons';
import request from 'superagent-bluebird-promise';

import {step} from '../lib/constant';

let config = null;

export default React.createClass({
  mixins: [React.addons.LinkedStateMixin],
  getInitialState() {
    let state = { config: 'Loading'};
    if (config != null) {
      state.config = config;
    }
    return state;
  },
  componentDidMount() {
    if (config == null) {
      request.get('/static/example_config.json').then(
        data => this.setState({ config: data.text })
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
  componentWillUnmount() {
    config = this.state.config;
  },
  render() {
    return (
      <div>
        <h1>Config</h1>
        <button onClick={this.handleGo}>Go</button>
        <br />
        <textarea valueLink={this.linkState('config')} style={{width:'600px',height:'500px'}}></textarea>
      </div>
    );
  }
});
