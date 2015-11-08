import React from 'react/addons';

import Config from './Config';
import Animation from './Animation';

export default React.createClass({
  componentWillMount() {
    this.handleToConfig();
  },
  handleToAnimation(frames) {
    let e = <Animation handleToConfig={this.handleToConfig} frames={frames} />;
    this.setState({element: e});
  },
  handleToConfig() {
    let e = <Config handleToAnimation={this.handleToAnimation} />;
    this.setState({element: e});
  },
  render() {
    return this.state.element;
  }
});
