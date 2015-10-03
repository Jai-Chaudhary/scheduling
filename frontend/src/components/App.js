import React from 'react/addons';

import Config from './Config';
import Playroom from './Playroom';
import Animation from './Animation';

export default React.createClass({
  componentWillMount() {
    this.handleToConfig();
  },
  handleToPlayroom(frames, lBits) {
    let e = <Playroom handleToConfig={this.handleToConfig} frames={frames} lBits={lBits} />;
    this.setState({element: e});
  },
  handleToConfig() {
    let e = <Config handleToPlayroom={this.handleToPlayroom} />;
    this.setState({element: e});
  },
  render() {
    return this.state.element;
  }
});
