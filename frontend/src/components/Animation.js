import React from 'react/addons';

import {avgStat, toTime, ganttHelper} from '../lib/util';

export default React.createClass({
  render() {
    const frame = this.props.frame;

    return (
        <div>
          <h1>Animation</h1>
          <button onClick={this.props.handleToConfig}>Go to Config</button>
          <button onClick={this.props.handleToSandbox}>Sandbox</button>
          {this.props.idSlider}

          <div>
            Avg waiting time: {avgStat(frame.stats)}
          </div>

          {ganttHelper(frame.state, frame.animation, frame.stats)}
        </div>
    );
  }
});
