import React from 'react/addons';
import request from 'superagent-bluebird-promise';

import {step} from '../lib/constant';

let config = null;

export default React.createClass({
  mixins: [React.addons.LinkedStateMixin],
  getInitialState() {
    let state = { config: 'Loading' };
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
    request.post('/parse_synthetic')
      .send(JSON.parse(this.state.config)).then(
        res => {
          const state = res.body;
          return request.post('/simulate_frames')
          .send(state).promise();
        }
      ).then(
        res => {
          return this.props.handleToAnimation(res.body);
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

      </div>
    );
  }
});
