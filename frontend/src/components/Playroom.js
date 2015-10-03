import React from 'react/addons';

import Config from './Config';
import Animation from './Animation';
import Sandbox from './Sandbox';

export default React.createClass({
  getInitialState() {
    return { page: 'animation', frames: this.props.frames, id: 0};
  },
  updateFrames(frames) {
    this.setState({
      frames: this.state.frames.slice(0, this.state.id).concat(frames),
      page: 'animation'
    });
  },
  getSandbox() {
    let frames = this.state.frames;
    let id = this.state.id;

    return <Sandbox
      frame={frames[id]}
      lBits={this.props.lBits}
      handleToAnimation={() => this.setState({page: 'animation'})}
      updateFrames={this.updateFrames}
    />;
  },
  getAnimation() {
    let frames = this.state.frames;
    let id = this.state.id;

    let idSlider = <input type='range'
      min='0' max={frames.length-1} value={id}
      onChange={ evt=>
        this.setState({
          id: parseInt(evt.target.value)
        })
      }
      style={{width: '300px'}}
    />;

    return <Animation
      frame={frames[id]}
      idSlider={idSlider}
      handleToConfig={this.props.handleToConfig}
      handleToSandbox={() => this.setState({page: 'sandbox'})}
    />;
  },
  render() {
    if (this.state.page == 'animation') {
      return this.getAnimation();
    } else {
      return this.getSandbox();
    }
  }
});
